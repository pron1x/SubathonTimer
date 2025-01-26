package com.pronixxx.subathon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.data.entity.*;
import com.pronixxx.subathon.data.repository.TimerEventRepository;
import com.pronixxx.subathon.datamodel.*;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<String, TimerEvent> timers = new HashMap<>();

    @PostConstruct
    public void init() {
        getLogger().debug("Initializing timer.");
        List<TimerEntity> timerList = new ArrayList<>();

        if(timerList.isEmpty()) {
            getLogger().info("Timer list is empty.");
            getLogger().info("Initializing timer for 'TEST'.");
            initializeTimer("TEST");
        } else {
            for(TimerEntity timerEntity : timerList) {
                Timer resultingTimer = mapper.map(timerEntity, Timer.class);
                // TODO: Change this to use channel ID!
                //timers.put(resultingTimer.getChannelId(), resultingTimer);
                if(resultingTimer.getState() == TICKING || resultingTimer.getState() == PAUSED) {
                    // Schedule timerControl here!
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
        TimerEvent initialEvent = new TimerEvent();
        initialEvent.setType(TimerEventType.STATE_CHANGE);
        initialEvent.setOldTimerState(UNINITIALIZED);
        initialEvent.setCurrentTimerState(INITIALIZED);

        Instant now = Instant.now();
        initialEvent.setStartTime(now);
        initialEvent.setCurrentEndTime(now.plusSeconds(INITIAL_TIMER_SECONDS));

        initialEvent.setTimestamp(now);

        TimerEventEntity eventEntity = timerEventRepository.save(mapper.map(initialEvent, TimerEventEntity.class));
        timers.put(channelId, mapper.map(eventEntity, TimerEvent.class));
        //return mapper.map(eventEntity, TimerEvent.class);
    }

    public void startTimer(String channelId, SubathonCommandEvent command) {
        TimerEvent lastEvent = timers.get(channelId);
        if(lastEvent.getCurrentTimerState() != INITIALIZED) {
            getLogger().warn("Cannot start the timer if it is not initialized or already started. A started timer has to be resumed!");
            return;
        }
        getLogger().debug("Starting timer");
        Instant now = Instant.now();
        TimerEvent timerEvent = createTimerEvent(lastEvent, TimerEventType.STATE_CHANGE,
                TICKING,
                now.plusSeconds(INITIAL_TIMER_SECONDS));

        timerEvent.setStartTime(now);
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));
        TimerEventEntity entity = saveTimerEventToDatabase(toSave);

        timers.put(channelId, mapper.map(entity, TimerEvent.class));
        lastEvent = mapper.map(entity, TimerEvent.class);

        timerControl.scheduleCommand(channelId, () -> stopTimer(channelId), lastEvent.getCurrentEndTime());
        timerControl.setPaused(channelId, false);
        publishEvent(lastEvent);
        getLogger().info("Timer started. [Start: {}, End: {}]", lastEvent.getStartTime(), lastEvent.getCurrentEndTime());
    }

    public void pauseTimer(String channelId, SubathonCommandEvent command) {
        TimerEvent lastEvent = timers.get(channelId);
        if(lastEvent.getCurrentTimerState() != TICKING) {
            getLogger().info("Not pausing a not ticking timer. Ignoring!");
            return;
        }
        getLogger().debug("Pausing timer");
        TimerEvent timerEvent = createTimerEvent(lastEvent, TimerEventType.STATE_CHANGE, PAUSED, lastEvent.getCurrentEndTime());
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));

        TimerEventEntity entity = saveTimerEventToDatabase(toSave);

        timers.put(channelId, mapper.map(entity, TimerEvent.class));

        lastEvent = mapper.map(entity, TimerEvent.class);
        timerControl.setPaused(channelId, true);
        publishEvent(lastEvent);
    }

    private void resumeTimer(String channelId, SubathonCommandEvent command) {
        TimerEvent lastEvent = timers.get(channelId);
        if(lastEvent.getCurrentTimerState() != PAUSED) {
            getLogger().info("Not resuming a not ticking timer. Ignoring!");
            return;
        }
        getLogger().debug("Resuming timer!");
        // Calculate the seconds the timer has been paused for to get new end time
        Duration d = Duration.between(lastEvent.getTimestamp(), lastEvent.getCurrentEndTime());
        Instant newEnd = Instant.now().plusSeconds(d.getSeconds());
        TimerEvent timerEvent = createTimerEvent(lastEvent, TimerEventType.STATE_CHANGE, TICKING, newEnd);
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));

        TimerEventEntity entity = saveTimerEventToDatabase(toSave);

        timers.put(channelId, mapper.map(entity, TimerEvent.class));
        lastEvent = mapper.map(entity, TimerEvent.class);
        timerControl.setExecutionTime(channelId, newEnd);
        timerControl.setPaused(channelId, false);
        publishEvent(lastEvent);
    }

    public void stopTimer(String channelId) {
        TimerEvent lastEvent = timers.get(channelId);
        getLogger().debug("Stopping timer!");
        Instant now = Instant.now();
        TimerEvent timerEvent = createTimerEvent(lastEvent, TimerEventType.STATE_CHANGE, ENDED, now);

        TimerEventEntity entity = saveTimerEventToDatabase(mapper.map(timerEvent, TimerEventEntity.class));

        // Probably want to remove that timer from the map instead of keeping an ended one!
        timers.put(channelId, mapper.map(entity, TimerEvent.class));
        lastEvent = mapper.map(entity, TimerEvent.class);
        publishEvent(lastEvent);

        getLogger().info("Stopped timer at {}. End timestamp: {}", timerEvent.getTimestamp(), timerEvent.getCurrentEndTime());
    }

    public void executeBotCommand(String channelId, SubathonCommandEvent command) {
        TimerEvent lastEvent = timers.get(channelId);
        getLogger().debug("Executing bot command: {}", command);
        switch (command.getCommand()) {
            case START -> {
                if(lastEvent.getCurrentTimerState() == INITIALIZED) {
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
        TimerEvent lastEvent = timers.get(channelId);
        if(lastEvent.getCurrentTimerState() != TICKING && lastEvent.getCurrentTimerState() != PAUSED) {
            getLogger().info("Not adding time to timer because it is {}. Ignoring {}.", lastEvent.getCurrentTimerState(), event);
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

        if (lastEvent.getCurrentTimerState() == PAUSED) {
            // Calculate extra duration in case the timer is paused before adding the event time
            Duration d = Duration.between(lastEvent.getTimestamp(), Instant.now());
            seconds += d.getSeconds();
        }

        long secondsToAdd = (long) Math.ceil(seconds);
        // Add the seconds from the event
        Instant newEnd = lastEvent.getCurrentEndTime().plusSeconds(secondsToAdd);
        TimerEvent timerEvent = createTimerEvent(lastEvent, TimerEventType.TIME_ADDITION, lastEvent.getCurrentTimerState(), newEnd);

        // TODO: Fix timerControl
        timerControl.setExecutionTime(channelId, timerEvent.getCurrentEndTime());

        getLogger().info("Added {} seconds for event {}", secondsToAdd, event);

        TimerEventEntity timerEventEntity = mapper.map(timerEvent, TimerEventEntity.class);
        timerEventEntity.setSubathonEvent(entity);
        TimerEventEntity savedEntity = saveTimerEventToDatabase(timerEventEntity);

        timers.put(channelId, mapper.map(savedEntity, TimerEvent.class));

        lastEvent = mapper.map(savedEntity, TimerEvent.class);
        publishEvent(lastEvent);
    }

    private void subtractSubathonEventTime(String channelId, SubathonCommandEvent command) {
        TimerEvent lastEvent = timers.get(channelId);
        if(lastEvent.getCurrentTimerState() != TICKING && lastEvent.getCurrentTimerState() != PAUSED) {
            getLogger().info("Not removing time from timer because it is {}. Ignoring {}.", lastEvent.getCurrentTimerState(), command);
            return;
        }
        getLogger().debug("Removing time from the timer.");
        long seconds = 0;
        if(lastEvent.getCurrentTimerState() == PAUSED) {
            Duration d = Duration.between(lastEvent.getTimestamp(), Instant.now());
            seconds += d.getSeconds();
        }
        seconds -= command.getSeconds();
        Instant newEnd = lastEvent.getCurrentEndTime().plusSeconds(seconds);
        if(newEnd.isBefore(Instant.now())) {
            getLogger().info("Removing {} seconds from the timer would stop it, ignoring!", command.getSeconds());
            return;
        }
        TimerEvent timerEvent = createTimerEvent(lastEvent, TimerEventType.TIME_SUBTRACTION, lastEvent.getCurrentTimerState(), newEnd);

        timerControl.setExecutionTime(channelId, timerEvent.getCurrentEndTime());

        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));
        TimerEventEntity savedEntity = saveTimerEventToDatabase(toSave);

        timers.put(channelId, mapper.map(savedEntity, TimerEvent.class));
        lastEvent = mapper.map(savedEntity, TimerEvent.class);
        publishEvent(lastEvent);
    }

    // TODO: Simplify this by creating a 'TimerEvent.nextEvent()' method that copies the current values into the old values of a new Object
    private TimerEvent createTimerEvent(TimerEvent lastEvent, TimerEventType type, TimerState newTimerState, Instant newEndTime) {
        TimerEvent timerEvent = new TimerEvent();

        timerEvent.setType(type);
        timerEvent.setTimestamp(Instant.now());
        timerEvent.setStartTime(lastEvent.getStartTime());

        timerEvent.setOldTimerState(lastEvent.getCurrentTimerState());
        timerEvent.setOldEndTime(lastEvent.getCurrentEndTime());

        timerEvent.setCurrentTimerState(newTimerState);
        timerEvent.setCurrentEndTime(newEndTime);

        return timerEvent;
    }

    private TimerEventEntity saveTimerEventToDatabase(TimerEventEntity timerEvent) {
        return timerEventRepository.save(timerEvent);
    }

    public TimerEvent getLastEventForChannel(String channelId) {
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
