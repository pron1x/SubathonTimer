package com.pronixxx.subathon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.data.entity.*;
import com.pronixxx.subathon.data.repository.TimerRepository;
import com.pronixxx.subathon.datamodel.*;
import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.datamodel.enums.SubTier;
import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.executor.AdjustableScheduledExecutorService;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pronixxx.subathon.datamodel.enums.TimerState.*;

@Service
public class TimerService implements HasLogger {

    @Autowired
    RabbitMessageService messageService;

    @Autowired
    TimerRepository timerRepository;

    @Autowired
    TimerEventService timerEventService;

    @Autowired
    ModelMapper mapper;

    @Autowired
    ObjectMapper objectMapper;

    AdjustableScheduledExecutorService timerControl = new AdjustableScheduledExecutorService();

    @Value("${timer.seconds.follow}")
    private long FOLLOWER_SECONDS = 10;
    @Value("${timer.seconds.raid}")
    private long RAIDER_SECONDS = 1;
    @Value("${timer.seconds.tier1}")
    private long TIER_1_SECONDS = 300;
    @Value("${timer.seconds.tier2}")
    private long TIER_2_SECONDS = 600;
    @Value("${timer.seconds.tier3}")
    private long TIER_3_SECONDS = 1500;
    @Value("${timer.seconds.tier1-gift}")
    private long TIER_1_GIFT_SECONDS = 300;
    @Value("${timer.seconds.tier2-gift}")
    private long TIER_2_GIFT_SECONDS = 600;
    @Value("${timer.seconds.tier3-gift}")
    private long TIER_3_GIFT_SECONDS = 1500;
    @Value("${timer.seconds.euro}")
    private long EURO_SECONDS = 60; // Seconds added per 100 Euro cents
    @Value("${timer.seconds.bits}")
    private long BITS_SECONDS = 60; // Seconds added per 100 bits
    @Value("${timer.seconds.initial}")
    private long INITIAL_TIMER_SECONDS = 100;

    /*  Instead of lastEvent we use the correct timer instance -> Keeps track of state, start and end time
        Update the timer instance on change -> State change, end change etc.
        -> Create event for that event, save to database with reference to the timer
            (Performance problems due to two changes per event? Update AND Insert needed....)

        Need the ID of the timer that the event is for -> Get ID from channel name/id? Ignore Timer id and get them from
        channel id all the time?
        Keep (active) timers in hashmap with user id -> timer
        If no timer -> Initialize new timer. Else use the timer
        On boot up, find all timers with status NOT ended -> Put in hashmap. ONLY ONE TIMER PER USER ID!!!
    */
    private final Map<String, Timer> timers = new HashMap<>();

    @PostConstruct
    public void init() {
        getLogger().debug("Initializing timer.");
        List<TimerEntity> timerList = timerRepository.findByStateIsNot(ENDED);
        getLogger().info("Found the following timers: {}", timerList);

        // This will check if a timer exists and schedule the end time for it
        for(TimerEntity timerEntity : timerList) {
            Timer timer = mapper.map(timerEntity, Timer.class);
            // TODO: Change this to use channel ID!
            timers.put(timer.getChannelName(), timer);
            if(timer.getState() == TICKING || timer.getState() == PAUSED) {
                timerControl.scheduleCommand(timer.getChannelName(), () -> stopTimer(timer.getChannelName()), timer.getEndTime());
                timerControl.setPaused(timer.getChannelName(), timer.getState() != TICKING);
            }
        }
        // FIXME: Only for testing, initialize a new test timer here if list is empty!
        if(timerList.isEmpty()) {
            getLogger().info("Timer list is empty.");
            getLogger().info("Initializing timer for 'TEST'.");
            // Initialize new Timer for testing purposes
            initializeTimer("TEST");
        }
    }


    public void initializeTimer(String channelId) {
        // Create new timer object only if it doesn't exist yet
        if(timers.containsKey(channelId)) {
            getLogger().info("Timer for channel id '{}'already exists.", channelId);
            return;
        }
        // Create new timer and assign (currently random) ID (is ID filled by JPA on object creation?)
        Timer timer = new Timer();
        timer.setChannelName(channelId);
        // Set timer status, start and end time not needed yet
        Instant now = Instant.now();
        timer.setState(INITIALIZED);
        timer.setUpdateTime(now);

        // Create initial event for the timer and set timer ID
        TimerEvent initialEvent = new TimerEvent();
        initialEvent.setType(TimerEventType.STATE_CHANGE);
        initialEvent.setOldTimerState(UNINITIALIZED);
        initialEvent.setCurrentTimerState(INITIALIZED);

        initialEvent.setTimestamp(now);

        TimerEntity timerEntity = timerRepository.save(mapper.map(timer, TimerEntity.class));
        initialEvent.setTimerId(timerEntity.getId());
        timers.put(timerEntity.getChannelName(), mapper.map(timerEntity, Timer.class));
        timerEventService.save(initialEvent);
    }

    public void startTimer(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        if(timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to start it!", channelId);
            return;
        }
        if(timer.getState() != INITIALIZED) {
            getLogger().warn("Cannot start the timer if it is not initialized or has already started. A started timer has to be resumed!");
            return;
        }
        getLogger().debug("Starting timer");
        Instant now = Instant.now();
        // Create new TimerEvent with previous timer data
        TimerEvent timerEvent = timerEventService.createNewTimerEvent(timer.getId(), TimerEventType.STATE_CHANGE,
                timer.getState(), TICKING,
                timer.getEndTime(), now.plusSeconds(INITIAL_TIMER_SECONDS), command);

        // Adjust timer to started
        timer.setStartTime(now);
        timer.setUpdateTime(now);
        timer.setState(TICKING);
        timer.setEndTime(now.plusSeconds(INITIAL_TIMER_SECONDS));
        timers.put(timer.getChannelName(), timer);

        // Schedule `stopTimer` command
        timerControl.scheduleCommand(channelId, () -> stopTimer(channelId), timer.getEndTime());
        timerControl.setPaused(channelId, false);

        timerRepository.save(mapper.map(timer, TimerEntity.class));
        // Save and publish timer event
        publishEvent(timerEventService.save(timerEvent));
        getLogger().info("Timer started. [Start: {}, End: {}]", timer.getStartTime(), timer.getEndTime());
    }

    public void pauseTimer(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        if(timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to pause it!", channelId);
            return;
        }
        // Only pause a ticking timer
        if(timer.getState() != TICKING) {
            getLogger().info("Not pausing a not ticking timer. Ignoring!");
            return;
        }
        getLogger().debug("Pausing timer");
        // Pause scheduled execution
        timerControl.setPaused(channelId, true);

        // Create timer event
        TimerEvent timerEvent = timerEventService.createNewTimerEvent(timer.getId(),
                TimerEventType.STATE_CHANGE,
                timer.getState(), PAUSED,
                timer.getEndTime(), timer.getEndTime(), command);

        // Adjust timer status and save it
        timer.setState(TimerState.PAUSED);
        timer.setUpdateTime(Instant.now());
        timers.put(timer.getChannelName(), timer);
        timerRepository.save(mapper.map(timer, TimerEntity.class));

        // Save and publish timer event
        publishEvent(timerEventService.save(timerEvent));
    }

    private void resumeTimer(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        if(timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to resume it!", channelId);
            return;
        }
        // Only resume timer if it is paused
        if(timer.getState() != PAUSED) {
            getLogger().info("Not resuming a not ticking timer. Ignoring!");
            return;
        }
        getLogger().debug("Resuming timer!");

        // Calculate the seconds the timer has been paused for to get new end time
        Duration d = Duration.between(timer.getUpdateTime(), timer.getEndTime());

        // Calculate new end
        Instant now = Instant.now();
        Instant newEnd = now.plusSeconds(d.getSeconds());

        // Create timer event
        TimerEvent timerEvent = timerEventService.createNewTimerEvent(timer.getId(),
                TimerEventType.STATE_CHANGE, timer.getState(), TICKING,
                timer.getEndTime(), newEnd, command);

        // Set new end in timer
        timer.setEndTime(newEnd);
        timer.setState(TICKING);
        timer.setUpdateTime(now);
        timers.put(timer.getChannelName(), timer);
        timerRepository.save(mapper.map(timer, TimerEntity.class));

        // Resume execution with new end
        timerControl.setExecutionTime(channelId, timer.getEndTime());
        timerControl.setPaused(channelId, false);

        // Save and publish timer event
        publishEvent(timerEventService.save(timerEvent));
    }

    public void stopTimer(String channelId) {
        Timer timer = timers.get(channelId);
        if(timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to stop!", channelId);
            return;
        }
        getLogger().debug("Stopping timer!");

        // Set end time
        Instant end = Instant.now();
        // Create timer event
        TimerEvent timerEvent = timerEventService.createNewTimerEvent(timer.getId(),
                TimerEventType.STATE_CHANGE, timer.getState(), ENDED,
                timer.getEndTime(), end, null);

        timer.setState(ENDED);
        timer.setEndTime(end);
        timer.setUpdateTime(end);

        // Remove timer from map, since it ended! New timer for channel can be initialized
        timers.remove(channelId);
        timerRepository.save(mapper.map(timer, TimerEntity.class));

        // Save and publish timer event
        publishEvent(timerEventService.save(timerEvent));

        getLogger().info("Stopped timer at {}. End timestamp: {}", timerEvent.getTimestamp(), timer.getEndTime());
    }

    public void executeBotCommand(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        if(timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to execute command '{}'!", channelId, command);
            return;
        }
        getLogger().debug("Executing bot command: {}", command);
        switch (command.getCommand()) {
            case START -> {
                if(timer.getState() == INITIALIZED) {
                    startTimer(channelId, command);
                } else {
                    resumeTimer(channelId, command);
                }
            }
            case PAUSE -> pauseTimer(channelId, command);
            case ADD -> addSubathonEventTime(channelId, command);
            case REMOVE -> subtractSubathonEventTime(channelId, command);
            default -> getLogger().warn("Command {} not yet implemented!", command.getCommand());
        }
    }
    
    public void addSubathonEventTime(String channelId, SubathonEvent event) {
        Timer timer = timers.get(channelId);
        if(timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to handle subathon event '{}'!", channelId, event);
            return;
        }

        if(timer.getState() != TICKING && timer.getState() != PAUSED) {
            getLogger().info("Not adding time to timer because it is {}. Ignoring {}.", timer.getState(), event);
            return;
        }
        getLogger().debug("Adding time for event: {}", event);

        double seconds = switch (event.getType()) {
            case FOLLOW -> FOLLOWER_SECONDS;
            case RAID -> ((SubathonRaidEvent)event).getAmount() * RAIDER_SECONDS;
            case SUBSCRIPTION -> {
                SubathonSubEvent subEvent = (SubathonSubEvent) event;
                if(subEvent.isGifted()) {
                    yield subEvent.getTier() == SubTier.TIER_3 ? TIER_3_GIFT_SECONDS :
                            subEvent.getTier() == SubTier.TIER_2 ? TIER_2_GIFT_SECONDS : TIER_1_GIFT_SECONDS;
                } else {
                    yield subEvent.getTier() == SubTier.TIER_3 ? TIER_3_SECONDS :
                            subEvent.getTier() == SubTier.TIER_2 ? TIER_2_SECONDS : TIER_1_SECONDS;
                }
            }
            /* Since we cannot guarantee that community gift get send before the individual sub gifts, we add no time for them but only log!
            Time is added for the individual gifted subscriptions */
            case GIFT -> 0;
            case TIP -> EURO_SECONDS * ((SubathonTipEvent)event).getAmount();
            case CHEER -> BITS_SECONDS * (((SubathonBitCheerEvent)event).getAmount() / 100.0);
            case COMMAND -> ((SubathonCommandEvent) event).getSeconds();
        };

        if (timer.getState() == PAUSED) {
            // Calculate extra duration in case the timer is paused before adding the event time
            Duration d = Duration.between(timer.getUpdateTime(), Instant.now());
            seconds += d.getSeconds();
        }
        long secondsToAdd = (long) Math.ceil(seconds);

        // Calculate new end time
        Instant newEnd = timer.getEndTime().plusSeconds(secondsToAdd);

        // Create new timer event
        TimerEvent timerEvent = timerEventService.createNewTimerEvent(timer.getId(), TimerEventType.TIME_ADDITION,
                timer.getState(), timer.getState(),
                timer.getEndTime(), newEnd,
                event);

        // Add the seconds from the event and save timer
        timer.setEndTime(newEnd);
        timer.setUpdateTime(Instant.now());
        timers.put(channelId, timer);
        // Change scheduled timer
        timerControl.setExecutionTime(channelId, timer.getEndTime());
        getLogger().info("Added {} seconds for event {}", secondsToAdd, event);
        timerRepository.save(mapper.map(timer, TimerEntity.class));

        // Save and publish timerEvent
        publishEvent(timerEventService.save(timerEvent));
    }

    private void subtractSubathonEventTime(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        if(timer == null) {
            getLogger().info("Timer for channel id '{}' not found, not able to execute subtract command '{}'!", channelId, command);
            return;
        }

        if(timer.getState() != TICKING && timer.getState() != PAUSED) {
            getLogger().info("Not removing time from timer because it is {}. Ignoring {}.", timer.getState(), command);
            return;
        }
        getLogger().debug("Removing time from the timer.");
        long seconds = 0;
        if(timer.getState() == PAUSED) {
            Duration d = Duration.between(timer.getUpdateTime(), Instant.now());
            seconds += d.getSeconds();
        }
        seconds -= command.getSeconds();
        Instant newEnd = timer.getEndTime().plusSeconds(seconds);
        if(newEnd.isBefore(Instant.now())) {
            getLogger().info("Removing {} seconds from the timer would stop it, ignoring!", command.getSeconds());
            return;
        }

        // Create timer event
        TimerEvent timerEvent = timerEventService.createNewTimerEvent(timer.getId(), TimerEventType.TIME_SUBTRACTION,
                timer.getState(), timer.getState(),
                timer.getEndTime(), newEnd, command);

        // Set new timer end and reschedule execution time
        timer.setEndTime(newEnd);
        timer.setUpdateTime(Instant.now());
        timers.put(channelId, timer);
        timerControl.setExecutionTime(channelId, timer.getEndTime());
        timerRepository.save(mapper.map(timer, TimerEntity.class));

        // Save and publish timer event
        publishEvent(timerEventService.save(timerEvent));
    }

    public Timer getTimerForChannel(String channelId) {
        return timers.get(channelId);
    }

    private void publishEvent(TimerEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            messageService.sendMessage(message);
        } catch (JsonProcessingException e) {
            getLogger().warn("Could not convert event to string! {}", event, e);
        } catch (AmqpException e) {
            getLogger().warn("Could not send message! {}", event, e);
        }
    }

}
