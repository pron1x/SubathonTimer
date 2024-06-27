package com.pronixxx.subathon.service;

import com.pronixxx.subathon.data.entity.EventEntity;
import com.pronixxx.subathon.data.entity.FollowEntity;
import com.pronixxx.subathon.data.entity.SubscribeEntity;
import com.pronixxx.subathon.data.entity.TimerEventEntity;
import com.pronixxx.subathon.data.repository.EventRepository;
import com.pronixxx.subathon.data.repository.TimerEventRepository;
import com.pronixxx.subathon.datamodel.*;
import com.pronixxx.subathon.datamodel.enums.SubTier;
import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.executor.AdjustableDateTimeExecuteControl;
import com.pronixxx.subathon.util.GlobalDefinition;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TimerService implements HasLogger {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    TimerEventRepository timerEventRepository;

    @Autowired
    ModelMapper mapper;

    AdjustableDateTimeExecuteControl timerControl;

    private final long FOLLOWER_SECONDS = 10;
    private final long SUB_BASE_SECONDS = 300;
    private final long INITIAL_TIMER_SECONDS = 100;

    Timer timer = new Timer();
    private TimerEvent lastEvent;

    @PostConstruct
    public void init() {
        getLogger().debug("Initializing timer.");
        TimerEventEntity event = timerEventRepository.findFirstByOrderByInsertTimeDescIdDesc();
        if(event == null) {
            getLogger().debug("Did not find previous timer event, initializing!");
            lastEvent = initializeTimer();
            startTimer();
        } else {
            getLogger().debug("Found previous timer event: {}", event);
            lastEvent = mapper.map(event, TimerEvent.class);
            timerControl = new AdjustableDateTimeExecuteControl(lastEvent.getCurrentEndTime(), lastEvent.getCurrentTimerState() != TimerState.TICKING);
        }
        timerControl.scheduleCommand(() -> {
            getLogger().warn("STOPPING THE TIMER NOW!");
            stopTimer();
        });
    }

    public TimerEvent initializeTimer() {
        TimerEvent initialEvent = new TimerEvent();
        initialEvent.setType(TimerEventType.STATE_CHANGE);
        initialEvent.setOldTimerState(TimerState.UNINITIALIZED);
        initialEvent.setCurrentTimerState(TimerState.INITIALIZED);
        initialEvent.setTimestamp(nowUTC());

        TimerEventEntity eventEntity = timerEventRepository.save(mapper.map(initialEvent, TimerEventEntity.class));
        return mapper.map(eventEntity, TimerEvent.class);
    }

    public void startTimer() {
        TimerEvent timerEvent = new TimerEvent();
        getLogger().debug("Starting timer");
        LocalDateTime now = nowUTC();

        timerEvent.setType(TimerEventType.STATE_CHANGE);
        timerEvent.setTimestamp(now);

        timerEvent.setOldTimerState(lastEvent.getCurrentTimerState());
        timerEvent.setOldEndTime(lastEvent.getCurrentEndTime());

        timerEvent.setCurrentTimerState(TimerState.TICKING);
        timerEvent.setCurrentEndTime(now.plusSeconds(INITIAL_TIMER_SECONDS));

        timerEvent.setStartTime(now);

        TimerEventEntity entity = saveTimerEventToDatabase(mapper.map(timerEvent, TimerEventEntity.class));
        lastEvent = mapper.map(entity, TimerEvent.class);

        timerControl = new AdjustableDateTimeExecuteControl(lastEvent.getCurrentEndTime(), false);
        getLogger().info("Timer started. [Start: {}, End: {}]", lastEvent.getStartTime(), lastEvent.getCurrentEndTime());
    }

    public void stopTimer() {
        TimerEvent timerEvent = new TimerEvent();
        getLogger().debug("Stopping timer!");
        LocalDateTime now = nowUTC();

        timerEvent.setType(TimerEventType.STATE_CHANGE);
        timerEvent.setTimestamp(now);
        timerEvent.setStartTime(lastEvent.getStartTime());

        timerEvent.setOldTimerState(lastEvent.getCurrentTimerState());
        timerEvent.setOldEndTime(lastEvent.getCurrentEndTime());

        timerEvent.setCurrentEndTime(now);
        timerEvent.setCurrentTimerState(TimerState.ENDED);

        TimerEventEntity entity = saveTimerEventToDatabase(mapper.map(timerEvent, TimerEventEntity.class));
        lastEvent = mapper.map(entity, TimerEvent.class);
        timerControl.cancelCommand();

        getLogger().info("Stopped timer at {}. End timestamp: {}", timerEvent.getTimestamp(), timerEvent.getCurrentEndTime());
    }

    public void pauseTimer() {
        TimerEvent timerEvent = new TimerEvent();

        timerEvent.setOldTimerState(timer.getState());
        timerEvent.setOldEndTime(timer.getEndTime());

        getLogger().debug("Pausing timer. [End: {}]", timer.getEndTime());
        timer.setState(TimerState.PAUSED);
        timer.setLastUpdate(nowUTC());

        timerEvent.setCurrentTimerState(timer.getState());
        timerEvent.setCurrentEndTime(timer.getEndTime());

        timerEvent.setTimestamp(timer.getLastUpdate());
        timerEvent.setType(TimerEventType.STATE_CHANGE);
        getLogger().info("Paused timer. [End: {}, Last Update: {}]", timer.getEndTime(), timer.getLastUpdate());
    }
    
    public void addSubathonEventTime(SubathonEvent event) {
        LocalDateTime now = nowUTC();

        TimerEvent timerEvent = new TimerEvent();
        timerEvent.setType(TimerEventType.TIME_ADDITION);
        timerEvent.setTimestamp(now);

        // Set the old state and end time based on the last event
        timerEvent.setOldTimerState(lastEvent.getCurrentTimerState());
        timerEvent.setOldEndTime(lastEvent.getCurrentEndTime());

        // Set the current state and start time based on last event, since the state won't change here
        timerEvent.setCurrentTimerState(lastEvent.getCurrentTimerState());
        timerEvent.setStartTime(lastEvent.getStartTime());

        EventEntity entity = null;

        getLogger().debug("Adding time for event: {}", event);
        if (lastEvent.getCurrentTimerState() == TimerState.PAUSED) {
            // Calculate extra duration in case the timer is paused before adding the event time
            Duration d = Duration.between(lastEvent.getTimestamp() ,lastEvent.getCurrentEndTime());
            timerEvent.setCurrentEndTime(now.plusSeconds(d.getSeconds()));
        }
        else {
            // If the timer is ticking, we keep the current end time before adding the event time
            timerEvent.setCurrentEndTime(lastEvent.getCurrentEndTime());
        }

        double seconds = switch (event.getType()) {
            case SUBSCRIPTION -> {
                SubathonSubEvent subEvent = (SubathonSubEvent) event;
                entity = mapper.map(event, SubscribeEntity.class);
                yield subEvent.getTier() == SubTier.TIER_3 ? 3 * SUB_BASE_SECONDS :
                        subEvent.getTier() == SubTier.TIER_2 ? 2 * SUB_BASE_SECONDS : SUB_BASE_SECONDS;
            }
            case TIP -> 0.0;
            case FOLLOW -> {
                entity = mapper.map(event, FollowEntity.class);
                yield FOLLOWER_SECONDS;
            }
            case COMMAND -> 0.0;
        };
        long secondsToAdd = (long) Math.ceil(seconds);
        // Add the seconds from the event
        timerEvent.setCurrentEndTime(timerEvent.getCurrentEndTime().plusSeconds(secondsToAdd));

        timerControl.setExecutionTime(timerEvent.getCurrentEndTime());

        getLogger().info("Added {} seconds for event {}", secondsToAdd, event);

        TimerEventEntity timerEventEntity = mapper.map(timerEvent, TimerEventEntity.class);
        timerEventEntity.setSubathonEvent(entity);
        TimerEventEntity savedEntity = saveTimerEventToDatabase(timerEventEntity);
        lastEvent = mapper.map(savedEntity, TimerEvent.class);
    }

    private TimerEventEntity saveTimerEventToDatabase(TimerEventEntity timerEvent) {
        return timerEventRepository.save(timerEvent);
    }

    private LocalDateTime nowUTC() {
        return LocalDateTime.now(ZoneId.of(GlobalDefinition.TZ));
    }

}
