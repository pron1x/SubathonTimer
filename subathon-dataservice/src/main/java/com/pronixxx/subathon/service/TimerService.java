package com.pronixxx.subathon.service;

import com.pronixxx.subathon.data.entity.EventEntity;
import com.pronixxx.subathon.data.entity.FollowEntity;
import com.pronixxx.subathon.data.entity.SubscribeEntity;
import com.pronixxx.subathon.data.repository.EventRepository;
import com.pronixxx.subathon.datamodel.*;
import com.pronixxx.subathon.datamodel.enums.SubTier;
import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;
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
    ModelMapper mapper;

    private final long FOLLOWER_SECONDS = 10;
    private final long SUB_BASE_SECONDS = 300;

    Timer timer = new Timer();

    @PostConstruct
    public void init() {
        getLogger().debug("Initializing timer.");
        LocalDateTime now = nowUTC();
        timer.setStartTime(now);
        timer.setEndTime(now.plusMinutes(60));
        timer.setState(TimerState.INITIALIZED);
        timer.setLastUpdate(now);
        getLogger().info("Initialized timer. [Start: {}, End: {}]", timer.getStartTime(), timer.getEndTime());
        startTimer();
    }

    public void startTimer() {
        TimerEvent timerEvent = new TimerEvent();
        getLogger().debug("Starting timer");
        LocalDateTime now = nowUTC();

        timerEvent.setOldTimerState(timer.getState());
        timerEvent.setOldEndTime(timer.getEndTime());

        Duration duration = Duration.between(timer.getLastUpdate(), timer.getEndTime());

        timer.setStartTime(now);
        timer.setEndTime(now.plusSeconds(duration.getSeconds()));
        timer.setState(TimerState.TICKING);
        timer.setLastUpdate(now);

        timerEvent.setCurrentTimerState(timer.getState());
        timerEvent.setCurrentEndTime(timer.getEndTime());

        timerEvent.setTimestamp(timer.getLastUpdate());
        timerEvent.setType(TimerEventType.STATE_CHANGE);

        getLogger().info("Timer started. [Start: {}, End: {}]", timer.getStartTime(), timer.getEndTime());
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
        TimerEvent timerEvent = new TimerEvent();
        timerEvent.setOldTimerState(timer.getState());
        timerEvent.setOldEndTime(timer.getEndTime());

        EventEntity entity = null;
        
        LocalDateTime now = nowUTC();
        getLogger().debug("Adding time for event: {}", event);
        if (timer.getState() == TimerState.PAUSED) {
            timer.setEndTime(now.plusSeconds(timer.getDurationLastUpdateToEnd().getSeconds()));
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
        timer.setEndTime(timer.getEndTime().plusSeconds(secondsToAdd));
        timer.setLastUpdate(now);

        getLogger().info("Added {} seconds for event {}", secondsToAdd, event);
        timerEvent.setCurrentTimerState(timer.getState());
        timerEvent.setCurrentEndTime(timer.getEndTime());

        timerEvent.setTimestamp(timer.getLastUpdate());
        timerEvent.setType(TimerEventType.TIME_ADDITION);
        saveToDatabase(entity);
    }

    private EventEntity saveToDatabase(EventEntity event) {
        return eventRepository.save(event);
    }

    private LocalDateTime nowUTC() {
        return LocalDateTime.now(ZoneId.of(GlobalDefinition.TZ));
    }

}
