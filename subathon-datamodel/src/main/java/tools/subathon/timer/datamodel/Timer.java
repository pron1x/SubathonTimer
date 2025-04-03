package tools.subathon.timer.datamodel;

import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;

public class Timer {

    private long id;
    private String channelName;
    private String channelId;
    private Instant startTime;
    private Instant endTime;
    private TimerState state;
    private Instant updateTime;

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

    @Override
    public String toString() {
        return "Timer{" +
                "id=" + id +
                ", channelName='" + channelName + '\'' +
                ", channelId=" + channelId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", state=" + state +
                ", updateTime=" + updateTime +
                '}';
    }
}
