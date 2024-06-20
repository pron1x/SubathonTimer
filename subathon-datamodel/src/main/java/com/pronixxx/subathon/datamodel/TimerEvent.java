package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;

import java.time.LocalDateTime;

public class TimerEvent {

    private LocalDateTime timestamp;
    private TimerEventType type;
    private LocalDateTime oldEndTime;
    private LocalDateTime currentEndTime;
    private TimerState oldTimerState;
    private TimerState currentTimerState;
    private SubathonEvent subathonEvent;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TimerEventType getType() {
        return type;
    }

    public void setType(TimerEventType type) {
        this.type = type;
    }

    public LocalDateTime getOldEndTime() {
        return oldEndTime;
    }

    public void setOldEndTime(LocalDateTime oldEndTime) {
        this.oldEndTime = oldEndTime;
    }

    public LocalDateTime getCurrentEndTime() {
        return currentEndTime;
    }

    public void setCurrentEndTime(LocalDateTime currentEndTime) {
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
                ", subathonEvent=" + subathonEvent +
                '}';
    }
}
