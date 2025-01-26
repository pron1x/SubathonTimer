package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.TimerState;

import java.time.Instant;

public class Timer {

    private long id;
    private String channelName;
    private long channelId;
    private Instant startTime;
    private Instant endTime;
    private TimerState state;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    @Override
    public String toString() {
        return "Timer{" +
                "id=" + id +
                ", channelName='" + channelName + '\'' +
                ", channelId=" + channelId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", state=" + state +
                '}';
    }
}
