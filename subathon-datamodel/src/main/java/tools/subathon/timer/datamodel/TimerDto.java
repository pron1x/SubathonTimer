package tools.subathon.timer.datamodel;

import org.jspecify.annotations.NonNull;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;
import java.util.UUID;

public record TimerDto(
        UUID id,
        String channelId,
        String channelName,
        Instant startTime,
        Instant endTime,
        TimerState state,
        Instant updateTime,
        Integer monetizedSecondsPerPoint,
        long totalMonetizedExtensionSeconds,
        long points
) {
    @Override
    public @NonNull String toString() {
        return "TimerDto{" +
                "id=" + id +
                ", channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", state=" + state +
                ", updateTime=" + updateTime +
                ", monetizedSecondsPerPoint=" + monetizedSecondsPerPoint +
                ", totalMonetizedExtensionSeconds=" + totalMonetizedExtensionSeconds +
                ", points=" + points +
                '}';
    }
}
