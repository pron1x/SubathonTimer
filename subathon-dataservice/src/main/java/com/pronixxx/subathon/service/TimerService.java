package com.pronixxx.subathon.service;

import com.pronixxx.subathon.data.entity.EventEntity;
import com.pronixxx.subathon.data.entity.FollowEntity;
import com.pronixxx.subathon.data.entity.SubscribeEntity;
import com.pronixxx.subathon.data.repository.EventRepository;
import com.pronixxx.subathon.datamodel.SubathonFollowerEvent;
import com.pronixxx.subathon.datamodel.SubathonSubEvent;
import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.datamodel.TimerEvent;
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

    public void addFollowToTimer(SubathonFollowerEvent event) {
        TimerEvent timerEvent = new TimerEvent();
        if(timer.getState() == TimerState.INITIALIZED) {
            startTimer();
        }

        timerEvent.setOldTimerState(timer.getState());
        timerEvent.setOldEndTime(timer.getEndTime());

        LocalDateTime now = nowUTC();
        if (timer.getState() == TimerState.PAUSED) {
            Duration duration = Duration.between(timer.getLastUpdate(), timer.getEndTime());
            timer.setEndTime(now.plusSeconds(duration.getSeconds()));
        }
        getLogger().debug("Adding follow time. [End: {}]", timer.getEndTime());

        timer.setEndTime(timer.getEndTime().plusSeconds(FOLLOWER_SECONDS));
        timer.setLastUpdate(now);

        timerEvent.setCurrentTimerState(timer.getState());
        timerEvent.setCurrentEndTime(timer.getEndTime());

        timerEvent.setTimestamp(timer.getLastUpdate());
        timerEvent.setType(TimerEventType.TIME_ADDITION);

        eventRepository.save(mapper.map(event, FollowEntity.class));

        getLogger().info("Added follow time. [End: {}]", timer.getEndTime());
    }

    public void addSubscriptionToTimer(SubathonSubEvent event) {
        TimerEvent timerEvent = new TimerEvent();
        if(timer.getState() == TimerState.INITIALIZED) {
            startTimer();
        }

        timerEvent.setOldTimerState(timer.getState());
        timerEvent.setOldEndTime(timer.getEndTime());

        LocalDateTime now = nowUTC();
        if (timer.getState() == TimerState.PAUSED) {
            Duration duration = Duration.between(timer.getLastUpdate(), timer.getEndTime());
            timer.setEndTime(now.plusSeconds(duration.getSeconds()));
        }

        getLogger().debug("Adding subscription time. [Tier: {}, End: {}]", event.getTier(), timer.getEndTime());
        long seconds = event.getTier() == SubTier.TIER_3 ? 3 * SUB_BASE_SECONDS :
                event.getTier() == SubTier.TIER_2 ? 2 * SUB_BASE_SECONDS : SUB_BASE_SECONDS;
        timer.setEndTime(timer.getEndTime().plusSeconds(seconds));
        timer.setLastUpdate(now);

        timerEvent.setCurrentTimerState(timer.getState());
        timerEvent.setCurrentEndTime(timer.getEndTime());

        timerEvent.setTimestamp(timer.getLastUpdate());
        timerEvent.setType(TimerEventType.TIME_ADDITION);
        
        saveToDatabase(mapper.map(event, SubscribeEntity.class));

        getLogger().info("Added subscription time. [Tier: {}, End: {}]", event.getTier(), timer.getEndTime());
    }

    private EventEntity saveToDatabase(EventEntity event) {
        return eventRepository.save(event);
    }

    private LocalDateTime nowUTC() {
        return LocalDateTime.now(ZoneId.of(GlobalDefinition.TZ));
    }

}
