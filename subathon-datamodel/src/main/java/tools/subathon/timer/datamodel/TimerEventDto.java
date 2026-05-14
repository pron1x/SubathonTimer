package tools.subathon.timer.datamodel;

import org.jspecify.annotations.NonNull;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;
import java.util.UUID;

public record TimerEventDto(
        UUID timerId,
        String channelId,
        Instant timestamp,
        TimerEventType type,
        Instant oldEndTime,
        Instant currentEndTime,
        TimerState oldTimerState,
        TimerState currentTimerState,
        SubathonEvent subathonEvent,
        long oldPoints,
        long newPoints
)
{
    @Override
    public @NonNull String toString() {
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
                ", oldPoints=" + oldPoints +
                ", newPoints=" + newPoints +
                '}';
    }
}
