package tools.subathon.rpc.payload.channel;

public record CreateMessageEventSubscriptionPayload(String channelId) implements ChannelEventSubscriptionPayload {
}
