package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.EventType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "event_type", columnDefinition = "ENUM('FOLLOW','SUBSCRIPTION')", discriminatorType = DiscriminatorType.STRING)
public abstract class EventEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", columnDefinition = "ENUM('FOLLOW', 'SUBSCRIPTION'", insertable = false, updatable = false)
    private EventType eventType;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "username", length = 30)
    private String username;

    @Column(name = "insert_time")
    private LocalDateTime insertTime;

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(LocalDateTime insertTime) {
        this.insertTime = insertTime;
    }
}
