package tools.subathon.rpc.payload.timer;

import java.time.Instant;

@Deprecated
public record PauseTimerPayload(String channelId, String username, Instant timestamp, String source) implements TimerPayload {
}
