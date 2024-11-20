package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;

import java.time.Instant;

public class TimerEvent {

    private Instant timestamp;
    private TimerEventType type;
    private Instant oldEndTime;
    private Instant currentEndTime;
    private TimerState oldTimerState;
    private TimerState currentTimerState;
    private Instant startTime;
    private SubathonEvent subathonEvent;

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public TimerEventType getType() {
        return type;
    }

    public void setType(TimerEventType type) {
        this.type = type;
    }

    public Instant getOldEndTime() {
        return oldEndTime;
    }

    public void setOldEndTime(Instant oldEndTime) {
        this.oldEndTime = oldEndTime;
    }

    public Instant getCurrentEndTime() {
        return currentEndTime;
    }

    public void setCurrentEndTime(Instant currentEndTime) {
        this.currentEndTime = currentEndTime;
    }

    public TimerState getOldTimerState() {
        return oldTimerState;
    }

    public void setOldTimerState(TimerState oldTimerState) {
        this.oldTimerState = oldTimerState;
    }

    public TimerState getCurrentTimerState() {
        return currentTimerState;
    }

    public void setCurrentTimerState(TimerState currentTimerState) {
        this.currentTimerState = currentTimerState;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public SubathonEvent getSubathonEvent() {
        return subathonEvent;
    }

    public void setSubathonEvent(SubathonEvent subathonEvent) {
        this.subathonEvent = subathonEvent;
    }

    @Override
    public String toString() {
        return "TimerEvent{" +
                "timestamp=" + timestamp +
                ", type=" + type +
                ", oldEndTime=" + oldEndTime +
                ", currentEndTime=" + currentEndTime +
                ", oldTimerState=" + oldTimerState +
                ", currentTimerState=" + currentTimerState +
                ", startTime=" + startTime +
                ", subathonEvent=" + subathonEvent +
                '}';
    }
}
