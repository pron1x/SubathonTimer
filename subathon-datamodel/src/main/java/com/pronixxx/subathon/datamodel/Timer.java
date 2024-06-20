package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.TimerState;

import java.time.Duration;
import java.time.LocalDateTime;

public class Timer {
    private TimerState state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdate;

    public Timer() {
        state = TimerState.UNINITIALIZED;
    }

    public Duration getDurationStartToEnd() {
        return Duration.between(startTime, endTime);
    }

    public Duration getDurationLastUpdateToEnd() {
        return Duration.between(lastUpdate, endTime);
    }

    public TimerState getState() {
        return state;
    }

    public void setState(TimerState state) {
        this.state = state;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
