package com.pronixxx.subathon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.data.entity.*;
import com.pronixxx.subathon.data.repository.TimerEventRepository;
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
import java.util.*;

import static com.pronixxx.subathon.datamodel.enums.TimerState.*;

@Service
public class TimerService implements HasLogger {

    @Autowired
    RabbitMessageService messageService;

    @Autowired
    TimerEventRepository timerEventRepository;

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

    //private TimerEvent lastEvent;

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
    // TODO: Check if timers.get returns `null`
    // TODO: Change this to not include the last event but the general timer object
    private final Map<String, Timer> timers = new HashMap<>();

    @PostConstruct
    public void init() {
        getLogger().debug("Initializing timer.");
        List<TimerEntity> timerList = new ArrayList<>();

        // This will check if a timer exists and schedule the end time for it
        // TODO: Remove the if check and only use the loop, no  need to initialize a new timer here ever
        if(timerList.isEmpty()) {
            getLogger().info("Timer list is empty.");
            getLogger().info("Initializing timer for 'TEST'.");
            // Initialize new Timer for testing purposes
            initializeTimer("TEST");
        } else {
            for(TimerEntity timerEntity : timerList) {
                Timer resultingTimer = mapper.map(timerEntity, Timer.class);
                // TODO: Change this to use channel ID!
                timers.put(resultingTimer.getChannelName(), resultingTimer);
                if(resultingTimer.getState() == TICKING || resultingTimer.getState() == PAUSED) {
                    timerControl.scheduleCommand(resultingTimer.getChannelName(), () -> stopTimer(resultingTimer.getChannelName()), resultingTimer.getEndTime());
                    timerControl.setPaused(resultingTimer.getChannelName(), resultingTimer.getState() == PAUSED);
                }
            }
        }

        //TimerEventEntity event = timerEventRepository.findFirstByOrderByInsertTimeDescIdDesc();
        //if(event == null) {
        //    getLogger().debug("Did not find previous timer event, initializing!");
        //    lastEvent = initializeTimer();
        //} else {
        //    getLogger().debug("Found previous timer event: {}", event);
        //    lastEvent = mapper.map(event, TimerEvent.class);
        //    if(lastEvent.getCurrentTimerState() == TICKING || lastEvent.getCurrentTimerState() == PAUSED) {
        //        timerControl.setTimerPaused(lastEvent.getCurrentTimerState() != TICKING);
        //        timerControl.scheduleCommand(this::stopTimer, lastEvent.getCurrentEndTime());
        //    }
        //}
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
        timer.setId(UUID.randomUUID().node());
        // Set timer status, start and end time not needed yet
        Instant now = Instant.now();
        timer.setState(INITIALIZED);

        // Create initial event for the timer and set timer ID
        TimerEvent initialEvent = new TimerEvent();
        initialEvent.setTimerId(timer.getId());
        initialEvent.setType(TimerEventType.STATE_CHANGE);
        initialEvent.setOldTimerState(UNINITIALIZED);
        initialEvent.setCurrentTimerState(INITIALIZED);

        initialEvent.setTimestamp(now);

        timerEventRepository.save(mapper.map(initialEvent, TimerEventEntity.class));
        timers.put(timer.getChannelName(), timer);
    }

    public void startTimer(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        if(timer.getState() != INITIALIZED) {
            getLogger().warn("Cannot start the timer if it is not initialized or has already started. A started timer has to be resumed!");
            return;
        }
        getLogger().debug("Starting timer");
        Instant now = Instant.now();
        // Create new TimerEvent with previous timer data
        TimerEvent timerEvent = createTimerEvent(timer, TimerEventType.STATE_CHANGE,
                TICKING,
                now.plusSeconds(INITIAL_TIMER_SECONDS));

        // Adjust timer to started
        timer.setStartTime(now);
        timer.setState(TICKING);
        timer.setEndTime(now.plusSeconds(INITIAL_TIMER_SECONDS));
        timers.put(timer.getChannelName(), timer);

        // Save TimerEvent
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class)); // ObjectMapper gets confused with the nested object
        TimerEventEntity entity = saveTimerEventToDatabase(toSave);

        // Schedule `stopTimer` command
        timerControl.scheduleCommand(channelId, () -> stopTimer(channelId), timer.getEndTime());
        timerControl.setPaused(channelId, false);

        // Publish timer event
        publishEvent(mapper.map(entity, TimerEvent.class));
        getLogger().info("Timer started. [Start: {}, End: {}]", timer.getStartTime(), timer.getEndTime());
    }

    public void pauseTimer(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        // Only pause a ticking timer
        if(timer.getState() != TICKING) {
            getLogger().info("Not pausing a not ticking timer. Ignoring!");
            return;
        }
        getLogger().debug("Pausing timer");
        // Pause scheduled execution
        timerControl.setPaused(channelId, true);
        // Create timer event
        // TODO: Fix event to have correct 'old' values
        TimerEvent timerEvent = createTimerEvent(timer, TimerEventType.STATE_CHANGE, PAUSED, timer.getEndTime());
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));
        TimerEventEntity entity = saveTimerEventToDatabase(toSave);

        // Adjust timer status and save it
        timer.setState(TimerState.PAUSED);
        timers.put(timer.getChannelName(), timer);

        // publish timer event
        publishEvent(mapper.map(entity, TimerEvent.class));
    }

    private void resumeTimer(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        // Only resume timer if it is paused
        if(timer.getState() != PAUSED) {
            getLogger().info("Not resuming a not ticking timer. Ignoring!");
            return;
        }
        getLogger().debug("Resuming timer!");
        // Calculate the seconds the timer has been paused for to get new end time
        TimerEvent lastEvent = mapper.map(timerEventRepository.findFirstByTimerIdOrderByInsertTimeDesc(timer.getId()), TimerEvent.class);

        Duration d = Duration.between(lastEvent.getTimestamp(), timer.getEndTime());

        // Calculate and save new end
        Instant newEnd = Instant.now().plusSeconds(d.getSeconds());
        timer.setEndTime(newEnd);
        timer.setState(TICKING);
        timers.put(timer.getChannelName(), timer);

        // Resume execution with new end
        timerControl.setExecutionTime(channelId, timer.getEndTime());
        timerControl.setPaused(channelId, false);

        // Create, save and publish timer event
        // TODO: Fix timer event to have correct old values
        TimerEvent timerEvent = createTimerEvent(timer, TimerEventType.STATE_CHANGE, TICKING, timer.getEndTime());
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));

        TimerEventEntity entity = saveTimerEventToDatabase(toSave);
        publishEvent(mapper.map(entity, TimerEvent.class));
    }

    public void stopTimer(String channelId) {
        Timer timer = timers.get(channelId);
        getLogger().debug("Stopping timer!");

        // Set timer to stopped
        Instant now = Instant.now();
        timer.setState(ENDED);
        timer.setEndTime(now);

        // Remove timer from map, since it ended! New timer for channel can be initialized
        timers.remove(channelId);

        // Create timer event
        TimerEvent timerEvent = createTimerEvent(timer, TimerEventType.STATE_CHANGE, ENDED, timer.getEndTime());
        TimerEventEntity entity = saveTimerEventToDatabase(mapper.map(timerEvent, TimerEventEntity.class));

        publishEvent(mapper.map(entity, TimerEvent.class));

        getLogger().info("Stopped timer at {}. End timestamp: {}", timerEvent.getTimestamp(), timer.getEndTime());
    }

    public void executeBotCommand(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
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
        TimerEvent lastEvent = mapper.map(timerEventRepository.findFirstByTimerIdOrderByInsertTimeDesc(timer.getId()), TimerEvent.class);
        if(timer.getState() != TICKING && timer.getState() != PAUSED) {
            getLogger().info("Not adding time to timer because it is {}. Ignoring {}.", timer.getState(), event);
            return;
        }
        getLogger().debug("Adding time for event: {}", event);
        EventEntity entity;

        double seconds = switch (event.getType()) {
            case FOLLOW -> {
                entity = mapper.map(event, FollowEntity.class);
                yield FOLLOWER_SECONDS;
            }
            case RAID -> {
                entity = mapper.map(event, RaidEntity.class);
                yield ((SubathonRaidEvent)event).getAmount() * RAIDER_SECONDS;
            }
            case SUBSCRIPTION -> {
                SubathonSubEvent subEvent = (SubathonSubEvent) event;
                entity = mapper.map(event, SubscribeEntity.class);
                if(subEvent.isGifted()) {
                    yield subEvent.getTier() == SubTier.TIER_3 ? TIER_3_GIFT_SECONDS :
                            subEvent.getTier() == SubTier.TIER_2 ? TIER_2_GIFT_SECONDS : TIER_1_GIFT_SECONDS;
                } else {
                    yield subEvent.getTier() == SubTier.TIER_3 ? TIER_3_SECONDS :
                            subEvent.getTier() == SubTier.TIER_2 ? TIER_2_SECONDS : TIER_1_SECONDS;
                }
            }
            case GIFT -> {
                SubathonCommunityGiftEvent giftEvent = (SubathonCommunityGiftEvent) event;
                entity = mapper.map(event, CommunityGiftEntity.class);
                //SubTier tier = giftEvent.getTier();
                //long s = tier == SubTier.TIER_3 ? TIER_3_SECONDS : tier == SubTier.TIER_2 ? TIER_2_SECONDS : TIER_1_SECONDS;
                //yield s * giftEvent.getAmount();
                // Since we cannot guarantee that community gift get send before the individual sub gifts, we add no time for them but only log!
                // Time is added for the individual gifted subscriptions
                yield 0;
            }
            case TIP -> {
                entity = mapper.map(event, TipEntity.class);
                yield EURO_SECONDS * ((SubathonTipEvent)event).getAmount();
            }
            case CHEER -> {
                entity = mapper.map(event, CheerEntity.class);
                yield BITS_SECONDS * (((SubathonBitCheerEvent)event).getAmount() / 100.0);
            }
            case COMMAND -> {
                SubathonCommandEvent command = (SubathonCommandEvent) event;
                entity = mapper.map(event, CommandEntity.class);
                yield command.getSeconds();
            }
        };

        if (timer.getState() == PAUSED) {
            // Calculate extra duration in case the timer is paused before adding the event time
            Duration d = Duration.between(lastEvent.getTimestamp(), Instant.now());
            seconds += d.getSeconds();
        }

        long secondsToAdd = (long) Math.ceil(seconds);
        // Add the seconds from the event and save timer
        Instant newEnd = timer.getEndTime().plusSeconds(secondsToAdd);
        timer.setEndTime(newEnd);
        timers.put(channelId, timer);

        // Change scheduled timer
        timerControl.setExecutionTime(channelId, timer.getEndTime());
        getLogger().info("Added {} seconds for event {}", secondsToAdd, event);

        // Create, save and publish timerEvent
        // TODO: Fix event to have correct 'old' values
        TimerEvent timerEvent = createTimerEvent(timer, TimerEventType.TIME_ADDITION, timer.getState(), timer.getEndTime());

        TimerEventEntity timerEventEntity = mapper.map(timerEvent, TimerEventEntity.class);
        timerEventEntity.setSubathonEvent(entity);
        TimerEventEntity savedEntity = saveTimerEventToDatabase(timerEventEntity);

        publishEvent(mapper.map(savedEntity, TimerEvent.class));
    }

    private void subtractSubathonEventTime(String channelId, SubathonCommandEvent command) {
        Timer timer = timers.get(channelId);
        TimerEvent lastEvent = mapper.map(timerEventRepository.findFirstByTimerIdOrderByInsertTimeDesc(timer.getId()), TimerEvent.class);

        if(timer.getState() != TICKING && timer.getState() != PAUSED) {
            getLogger().info("Not removing time from timer because it is {}. Ignoring {}.", timer.getState(), command);
            return;
        }
        getLogger().debug("Removing time from the timer.");
        long seconds = 0;
        if(timer.getState() == PAUSED) {
            Duration d = Duration.between(lastEvent.getTimestamp(), Instant.now());
            seconds += d.getSeconds();
        }
        seconds -= command.getSeconds();
        Instant newEnd = timer.getEndTime().plusSeconds(seconds);
        if(newEnd.isBefore(Instant.now())) {
            getLogger().info("Removing {} seconds from the timer would stop it, ignoring!", command.getSeconds());
            return;
        }

        // Set new timer end and reschedule execution time
        timer.setEndTime(newEnd);
        timers.put(channelId, timer);
        timerControl.setExecutionTime(channelId, timer.getEndTime());

        // Create, save and publish timer event
        // TODO: Fix event to have correct 'old' values
        TimerEvent timerEvent = createTimerEvent(timer, TimerEventType.TIME_SUBTRACTION, timer.getState(), timer.getEndTime());


        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));
        TimerEventEntity savedEntity = saveTimerEventToDatabase(toSave);

        publishEvent(mapper.map(savedEntity, TimerEvent.class));
    }

    // TODO: Simplify this by creating a 'Timer.pause/resume/add/subtract' method that emits a correct TimerEvent
    private TimerEvent createTimerEvent(Timer timer, TimerEventType type, TimerState newTimerState, Instant newEndTime) {
        TimerEvent timerEvent = new TimerEvent();

        timerEvent.setType(type);
        timerEvent.setTimestamp(Instant.now());

        timerEvent.setOldTimerState(timer.getState());
        timerEvent.setOldEndTime(timer.getEndTime());

        timerEvent.setCurrentTimerState(newTimerState);
        timerEvent.setCurrentEndTime(newEndTime);

        timerEvent.setTimerId(timer.getId());

        return timerEvent;
    }

    private TimerEventEntity saveTimerEventToDatabase(TimerEventEntity timerEvent) {
        return timerEventRepository.save(timerEvent);
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
