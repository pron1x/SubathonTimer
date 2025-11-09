package tools.subathon.rpc.payload.timer;

import java.time.Instant;

public record InitTimerPayload(String channelId, String channelName, String username, Instant timestamp, String source) implements TimerPayload {
    
}
