package tools.subathon.rpc.payload.channel;

@Deprecated
public record CreateChannelEventsSubscriptionPayload(String broadcasterUserId) implements ChannelEventSubscriptionPayload {
}
