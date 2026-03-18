package tools.subathon.rpc.payload.channel;

public record CreateChannelEventsSubscriptionPayload(String broadcasterUserId) implements ChannelEventSubscriptionPayload {
}
