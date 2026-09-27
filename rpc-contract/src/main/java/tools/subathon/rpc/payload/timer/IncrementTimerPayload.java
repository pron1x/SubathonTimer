package tools.subathon.rpc.payload.timer;

import java.time.Instant;

@Deprecated
public record IncrementTimerPayload(String channelId, long seconds, String username, Instant timestamp, String source) implements TimerPayload {

}
