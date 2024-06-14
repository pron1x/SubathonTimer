package com.pronixxx.subathon.service;

import com.pronixxx.subathon.datamodel.SubathonFollowerEvent;
import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TimerService {

    private long FOLLOWER_SECONDS = 10;

    Timer timer = new Timer();

    @PostConstruct
    public void init() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        timer.setStartTime(now);
        timer.setEndTime(now.plusMinutes(60));
        timer.setState(TimerState.INITIALIZED);
    }

    public void startTimer() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        Duration duration = Duration.between(timer.getStartTime(), timer.getEndTime());

        timer.setStartTime(now);
        timer.setEndTime(now.plusSeconds(duration.getSeconds()));
        timer.setState(TimerState.TICKING);
    }

    public void addFollowToTimer(SubathonFollowerEvent event) {
        if(timer.getState() == TimerState.INITIALIZED) {
            startTimer();
        }
        timer.setEndTime(timer.getEndTime().plusSeconds(FOLLOWER_SECONDS));
    }

}
