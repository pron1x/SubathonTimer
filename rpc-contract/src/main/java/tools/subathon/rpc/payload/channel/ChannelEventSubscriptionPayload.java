package tools.subathon.rpc.payload.channel;

import tools.subathon.rpc.payload.RpcPayload;

public sealed interface ChannelEventSubscriptionPayload extends RpcPayload permits CreateChannelEventsSubscriptionPayload, CreateMessageEventSubscriptionPayload {
}
