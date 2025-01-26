package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.TimerState;
import jakarta.persistence.Entity;

import java.time.Instant;

@Entity
public class TimerEntity extends BaseEntity {

    private String channelName;
    private long channelId;
    private Instant startTime;
    private Instant endTime;
    private TimerState state;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
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
}
