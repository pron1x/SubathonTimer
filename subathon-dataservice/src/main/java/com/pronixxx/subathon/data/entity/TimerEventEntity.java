package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "timer_event")
public class TimerEventEntity extends BaseEntity {

    @Column(name = "timestamp")
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "ENUM('TIME_ADDITION', 'TIME_SUBTRACTION', 'STATE_CHANGE')")
    private TimerEventType type;

    @Column(name = "old_end_time")
    private Instant oldEndTime;

    @Column(name = "current_end_time")
    private Instant currentEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_timer_state", columnDefinition = "ENUM('UNINITIALIZED', 'INITIALIZED', 'PAUSED', 'TICKING', 'ENDED')")
    private TimerState oldTimerState;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_timer_state", columnDefinition = "ENUM('UNINITIALIZED', 'INITIALIZED', 'PAUSED', 'TICKING', 'ENDED')")
    private TimerState currentTimerState;

    @Column(name = "start_time")
    private Instant startTime;

    @OneToOne(targetEntity = EventEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    private EventEntity subathonEvent;

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

    public EventEntity getSubathonEvent() {
        return subathonEvent;
    }

    public void setSubathonEvent(EventEntity subathonEvent) {
        this.subathonEvent = subathonEvent;
    }

    @Override
    public String toString() {
        return "TimerEventEntity{" +
                "timestamp=" + timestamp +
                ", id=" + id +
                ", type=" + type +
                ", oldEndTime=" + oldEndTime +
                ", currentEndTime=" + currentEndTime +
                ", oldTimerState=" + oldTimerState +
                ", currentTimerState=" + currentTimerState +
                ", startTime=" + startTime +
                ", subathonEvent=" + subathonEvent +
                ", insertTime=" + insertTime +
                "} ";
    }
}
