package tools.subathon.rpc.payload;

import tools.subathon.rpc.payload.channel.ChannelEventSubscriptionPayload;

@Deprecated
public sealed interface RpcPayload permits ChannelEventSubscriptionPayload {
}
