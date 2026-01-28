package tools.subathon.timer.datamodel;

import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;

public record TimerDto(
        Long id,
        String channelId,
        String channelName,
        Instant startTime,
        Instant endTime,
        TimerState state,
        Instant updateTime
) {
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
