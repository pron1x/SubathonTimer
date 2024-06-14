package com.pronixxx.subathon.service;

import com.pronixxx.subathon.datamodel.SubathonFollowerEvent;
import com.pronixxx.subathon.datamodel.SubathonSubEvent;
import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.datamodel.enums.SubTier;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.util.GlobalDefinition;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TimerService implements HasLogger {

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
        getLogger().info("Initialized timer. [Start: {}, End: {}]", timer.getStartTime(), timer.getEndTime());
    }

    public void startTimer() {
        getLogger().debug("Starting timer");
        LocalDateTime now = nowUTC();
        Duration duration = Duration.between(timer.getStartTime(), timer.getEndTime());

        timer.setStartTime(now);
        timer.setEndTime(now.plusSeconds(duration.getSeconds()));
        timer.setState(TimerState.TICKING);
        getLogger().info("Timer started. [Start: {}, End: {}]", timer.getStartTime(), timer.getEndTime());
    }

    public void addFollowToTimer(SubathonFollowerEvent event) {
        if(timer.getState() == TimerState.INITIALIZED) {
            startTimer();
        }
        getLogger().debug("Adding follow time. [End: {}]", timer.getEndTime());
        timer.setEndTime(timer.getEndTime().plusSeconds(FOLLOWER_SECONDS));
        getLogger().info("Added follow time. [End: {}]", timer.getEndTime());
    }

    public void addSubscriptionToTimer(SubathonSubEvent event) {
        if(timer.getState() == TimerState.INITIALIZED) {
            startTimer();
        }

        getLogger().debug("Adding subscription time. [Tier: {}, End: {}]", event.getTier(), timer.getEndTime());
        long seconds = event.getTier() == SubTier.TIER_3 ? 3 * SUB_BASE_SECONDS :
                event.getTier() == SubTier.TIER_2 ? 2 * SUB_BASE_SECONDS : SUB_BASE_SECONDS;
        timer.setEndTime(timer.getEndTime().plusSeconds(seconds));
        getLogger().info("Added subscription time. [Tier: {}, End: {}]", event.getTier(), timer.getEndTime());
    }

    private LocalDateTime nowUTC() {
        return LocalDateTime.now(ZoneId.of(GlobalDefinition.TZ));
    }

}
