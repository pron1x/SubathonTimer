package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.EventType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "event")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", columnDefinition = "ENUM('FOLLOW', 'RAID', 'SUBSCRIPTION', 'GIFT', 'TIP', 'CHEER', 'COMMAND')", discriminatorType = DiscriminatorType.STRING)
public abstract class EventEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "ENUM('FOLLOW', 'RAID', 'SUBSCRIPTION', 'GIFT', 'TIP', 'CHEER', 'COMMAND')", insertable = false, updatable = false)
    private EventType type;

    @Column(name = "timestamp")
    private Instant timestamp;

    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "username", length = 30)
    private String username;

    public EventType getType() {
        return type;
    }

    public void setType(EventType eventType) {
        this.type = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
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

    @Override
    public String toString() {
        return "EventEntity{" +
                ", id=" + id +
                ", type=" + type +
                ", username='" + username + '\'' +
                ", source='" + source + '\'' +
                ", timestamp=" + timestamp +
                ", insertTime=" + insertTime +
                "} ";
    }
}
