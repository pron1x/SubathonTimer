package tools.subathon.rpc.payload.timer;

import java.time.Instant;

public record PauseTimerPayload(String channelId, String username, Instant timestamp, String source) implements TimerPayload {
}
