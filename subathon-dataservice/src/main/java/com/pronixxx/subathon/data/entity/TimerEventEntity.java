package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "timer_event")
public class TimerEventEntity extends BaseEntity {

    @Column(name = "timestamp")
    private Instant timestamp;

    @Column(name = "timer_id")
    private Long timerId;

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

    @OneToOne(targetEntity = EventEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    private EventEntity subathonEvent;

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimerId() {
        return timerId;
    }

    public void setTimerId(Long timerId) {
        this.timerId = timerId;
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
                ", timerId=" + timerId +
                ", type=" + type +
                ", oldEndTime=" + oldEndTime +
                ", currentEndTime=" + currentEndTime +
                ", oldTimerState=" + oldTimerState +
                ", currentTimerState=" + currentTimerState +
                ", subathonEvent=" + subathonEvent +
                "} " + super.toString();
    }
}
