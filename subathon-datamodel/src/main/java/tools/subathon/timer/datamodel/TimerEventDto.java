package tools.subathon.timer.datamodel;

import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;

public record TimerEventDto(
        Long timerId,
        String channelId,
        Instant timestamp,
        TimerEventType type,
        Instant oldEndTime,
        Instant currentEndTime,
        TimerState oldTimerState,
        TimerState currentTimerState,
        SubathonEvent subathonEvent
)
{
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
