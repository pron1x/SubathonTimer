package tools.subathon.timer.dataservice.data.domain;

import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;
import java.util.UUID;

public class TimerEvent {
    private UUID timerId;
    private String channelId;
    private Instant timestamp;
    private TimerEventType type;
    private Instant oldEndTime;
    private Instant currentEndTime;
    private TimerState oldTimerState;
    private TimerState currentTimerState;
    private SubathonEvent subathonEvent;

    public UUID getTimerId() {
        return timerId;
    }

    public void setTimerId(UUID timerId) {
        this.timerId = timerId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

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

    public SubathonEvent getSubathonEvent() {
        return subathonEvent;
    }

    public void setSubathonEvent(SubathonEvent subathonEvent) {
        this.subathonEvent = subathonEvent;
    }

    @Override
    public String toString() {
        return "TimerEvent{" +
                "timerId=" + timerId +
                ", channelId=" + channelId +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", oldEndTime=" + oldEndTime +
                ", currentEndTime=" + currentEndTime +
                ", oldTimerState=" + oldTimerState +
                ", currentTimerState=" + currentTimerState +
                ", subathonEvent=" + subathonEvent +
                '}';
    }
}
