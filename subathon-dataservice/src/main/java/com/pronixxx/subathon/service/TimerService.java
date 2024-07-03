package com.pronixxx.subathon.service;

import com.pronixxx.subathon.data.entity.*;
import com.pronixxx.subathon.data.repository.EventRepository;
import com.pronixxx.subathon.data.repository.TimerEventRepository;
import com.pronixxx.subathon.datamodel.*;
import com.pronixxx.subathon.datamodel.enums.SubTier;
import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.executor.AdjustableScheduledExecutorService;
import com.pronixxx.subathon.util.GlobalDefinition;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.pronixxx.subathon.datamodel.enums.TimerState.*;

@Service
public class TimerService implements HasLogger {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    TimerEventRepository timerEventRepository;

    @Autowired
    ModelMapper mapper;

    AdjustableScheduledExecutorService timerControl = new AdjustableScheduledExecutorService();

    private final long FOLLOWER_SECONDS = 10;
    private final long RAIDER_SECONDS = 1;
    private final long SUB_BASE_SECONDS = 300;
    private final long EURO_SECONDS = 60; // Seconds added per 100 Euro cents
    private final long BITS_SECONDS = 60; // Seconds added per 100 bits
    private final long INITIAL_TIMER_SECONDS = 100;

    private TimerEvent lastEvent;

    @PostConstruct
    public void init() {
        getLogger().debug("Initializing timer.");
        TimerEventEntity event = timerEventRepository.findFirstByOrderByInsertTimeDescIdDesc();
        if(event == null) {
            getLogger().debug("Did not find previous timer event, initializing!");
            lastEvent = initializeTimer();
        } else {
            getLogger().debug("Found previous timer event: {}", event);
            lastEvent = mapper.map(event, TimerEvent.class);
            if(lastEvent.getCurrentTimerState() == TICKING || lastEvent.getCurrentTimerState() == PAUSED) {
                timerControl.setTimerPaused(lastEvent.getCurrentTimerState() != TICKING);
                timerControl.scheduleCommand(this::stopTimer, lastEvent.getCurrentEndTime());
            }
        }
    }

    public TimerEvent initializeTimer() {
        TimerEvent initialEvent = new TimerEvent();
        initialEvent.setType(TimerEventType.STATE_CHANGE);
        initialEvent.setOldTimerState(UNINITIALIZED);
        initialEvent.setCurrentTimerState(INITIALIZED);
        initialEvent.setTimestamp(nowUTC());

        TimerEventEntity eventEntity = timerEventRepository.save(mapper.map(initialEvent, TimerEventEntity.class));
        return mapper.map(eventEntity, TimerEvent.class);
    }

    public void startTimer(SubathonCommandEvent command) {
        if(lastEvent.getCurrentTimerState() != INITIALIZED) {
            getLogger().warn("Cannot start the timer if it is not initialized or already started. A started timer has to be resumed!");
            return;
        }
        getLogger().debug("Starting timer");
        LocalDateTime now = nowUTC();
        TimerEvent timerEvent = createTimerEvent(TimerEventType.STATE_CHANGE,
                TICKING,
                now.plusSeconds(INITIAL_TIMER_SECONDS));

        timerEvent.setStartTime(now);
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));
        TimerEventEntity entity = saveTimerEventToDatabase(toSave);
        lastEvent = mapper.map(entity, TimerEvent.class);

        timerControl.setTimerPaused(false);
        timerControl.scheduleCommand(this::stopTimer, lastEvent.getCurrentEndTime());
        getLogger().info("Timer started. [Start: {}, End: {}]", lastEvent.getStartTime(), lastEvent.getCurrentEndTime());
    }

    public void pauseTimer(SubathonCommandEvent command) {
        if(lastEvent.getCurrentTimerState() != TICKING) {
            getLogger().info("Not pausing a not ticking timer. Ignoring!");
            return;
        }
        getLogger().debug("Pausing timer");
        TimerEvent timerEvent = createTimerEvent(TimerEventType.STATE_CHANGE, PAUSED, lastEvent.getCurrentEndTime());
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));

        TimerEventEntity entity = saveTimerEventToDatabase(toSave);
        lastEvent = mapper.map(entity, TimerEvent.class);
        timerControl.setTimerPaused(true);
    }

    private void resumeTimer(SubathonCommandEvent command) {
        if(lastEvent.getCurrentTimerState() != TICKING) {
            getLogger().info("Not resuming a not ticking timer. Ignoring!");
            return;
        }
        getLogger().debug("Resuming timer!");
        // Calculate the seconds the timer has been paused for to get new end time
        Duration d = Duration.between(lastEvent.getTimestamp(), lastEvent.getCurrentEndTime());
        LocalDateTime newEnd = nowUTC().plusSeconds(d.getSeconds());
        TimerEvent timerEvent = createTimerEvent(TimerEventType.STATE_CHANGE, TICKING, newEnd);
        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));

        TimerEventEntity entity = saveTimerEventToDatabase(toSave);
        lastEvent = mapper.map(entity, TimerEvent.class);
        timerControl.setExecutionTime(newEnd);
        timerControl.setTimerPaused(false);
    }

    public void stopTimer() {
        getLogger().debug("Stopping timer!");
        LocalDateTime now = nowUTC();
        TimerEvent timerEvent = createTimerEvent(TimerEventType.STATE_CHANGE, ENDED, now);

        TimerEventEntity entity = saveTimerEventToDatabase(mapper.map(timerEvent, TimerEventEntity.class));
        lastEvent = mapper.map(entity, TimerEvent.class);

        getLogger().info("Stopped timer at {}. End timestamp: {}", timerEvent.getTimestamp(), timerEvent.getCurrentEndTime());
    }

    public void executeBotCommand(SubathonCommandEvent command) {
        getLogger().debug("Executing bot command: {}", command);
        switch (command.getCommand()) {
            case START -> {
                if(lastEvent.getCurrentTimerState() == INITIALIZED) {
                    startTimer(command);
                } else {
                    resumeTimer(command);
                }
            }
            case PAUSE -> {
                pauseTimer(command);
            }
            case ADD -> {
                addSubathonEventTime(command);
            }
            case REMOVE -> {
                subtractSubathonEventTime(command);
            }
            default -> getLogger().warn("Command {} not yet implemented!", command.getCommand());
        }
    }
    
    public void addSubathonEventTime(SubathonEvent event) {
        if(lastEvent.getCurrentTimerState() != TICKING && lastEvent.getCurrentTimerState() != PAUSED) {
            getLogger().info("Not adding time to timer because it is {}. Ignoring {}.", lastEvent.getCurrentTimerState(), event);
            return;
        }
        getLogger().debug("Adding time for event: {}", event);
        EventEntity entity = null;

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
                yield subEvent.getTier() == SubTier.TIER_3 ? 3 * SUB_BASE_SECONDS :
                        subEvent.getTier() == SubTier.TIER_2 ? 2 * SUB_BASE_SECONDS : SUB_BASE_SECONDS;
            }
            case GIFT -> {
                SubathonCommunityGiftEvent giftEvent = (SubathonCommunityGiftEvent) event;
                entity = mapper.map(event, CommunityGiftEntity.class);
                SubTier tier = giftEvent.getTier();
                int mult = tier == SubTier.TIER_3 ? 3 : tier == SubTier.TIER_2 ? 2 : 1;
                yield mult * SUB_BASE_SECONDS * giftEvent.getAmount();
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
            Duration d = Duration.between(lastEvent.getTimestamp(), nowUTC());
            seconds += d.getSeconds();
        }

        long secondsToAdd = (long) Math.ceil(seconds);
        // Add the seconds from the event
        LocalDateTime newEnd = lastEvent.getCurrentEndTime().plusSeconds(secondsToAdd);
        TimerEvent timerEvent = createTimerEvent(TimerEventType.TIME_ADDITION, lastEvent.getCurrentTimerState(), newEnd);

        timerControl.setExecutionTime(timerEvent.getCurrentEndTime());

        getLogger().info("Added {} seconds for event {}", secondsToAdd, event);

        TimerEventEntity timerEventEntity = mapper.map(timerEvent, TimerEventEntity.class);
        timerEventEntity.setSubathonEvent(entity);
        TimerEventEntity savedEntity = saveTimerEventToDatabase(timerEventEntity);
        lastEvent = mapper.map(savedEntity, TimerEvent.class);
    }

    private void subtractSubathonEventTime(SubathonCommandEvent command) {
        if(lastEvent.getCurrentTimerState() != TICKING && lastEvent.getCurrentTimerState() != PAUSED) {
            getLogger().info("Not removing time from timer because it is {}. Ignoring {}.", lastEvent.getCurrentTimerState(), command);
            return;
        }
        getLogger().debug("Removing time from the timer.");
        long seconds = 0;
        if(lastEvent.getCurrentTimerState() == PAUSED) {
            Duration d = Duration.between(lastEvent.getTimestamp(), nowUTC());
            seconds += d.getSeconds();
        }
        seconds -= command.getSeconds();
        LocalDateTime newEnd = lastEvent.getCurrentEndTime().plusSeconds(seconds);
        if(newEnd.isBefore(nowUTC())) {
            getLogger().info("Removing {} seconds from the timer would stop it, ignoring!", command.getSeconds());
            return;
        }
        TimerEvent timerEvent = createTimerEvent(TimerEventType.TIME_SUBTRACTION, lastEvent.getCurrentTimerState(), newEnd);

        timerControl.setExecutionTime(timerEvent.getCurrentEndTime());

        TimerEventEntity toSave = mapper.map(timerEvent, TimerEventEntity.class);
        toSave.setSubathonEvent(mapper.map(command, CommandEntity.class));
        TimerEventEntity savedEntity = saveTimerEventToDatabase(toSave);
        lastEvent = mapper.map(savedEntity, TimerEvent.class);
    }

    private TimerEvent createTimerEvent(TimerEventType type, TimerState newTimerState, LocalDateTime newEndTime) {
        TimerEvent timerEvent = new TimerEvent();

        timerEvent.setType(type);
        timerEvent.setTimestamp(nowUTC());
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

    private LocalDateTime nowUTC() {
        return LocalDateTime.now(ZoneId.of(GlobalDefinition.TZ));
    }

}
