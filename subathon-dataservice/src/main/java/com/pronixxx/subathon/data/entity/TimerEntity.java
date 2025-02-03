package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.TimerState;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "timer")
public class TimerEntity extends BaseEntity {

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", columnDefinition = "ENUM('UNINITIALIZED', 'INITIALIZED', 'PAUSED', 'TICKING', 'ENDED')")
    private TimerState state;

    @Column(name = "update_time")
    private Instant updateTime;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public TimerState getState() {
        return state;
    }

    public void setState(TimerState state) {
        this.state = state;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }
}
