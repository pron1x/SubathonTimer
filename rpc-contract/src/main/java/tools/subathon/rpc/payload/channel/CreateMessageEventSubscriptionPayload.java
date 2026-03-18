package tools.subathon.rpc.payload.channel;

public record CreateMessageEventSubscriptionPayload(String broadcasterUserId) implements ChannelEventSubscriptionPayload {
}
