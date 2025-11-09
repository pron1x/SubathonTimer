package tools.subathon.rpc.payload.timer;

import java.time.Instant;

public record StartTimerPayload(String channelId, String username, Instant timestamp, String source) implements TimerPayload {
}
