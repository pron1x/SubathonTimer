package tools.subathon.rpc.payload;

import tools.subathon.rpc.payload.channel.ChannelEventSubscriptionPayload;
import tools.subathon.rpc.payload.configuration.ChannelConfigPayload;
import tools.subathon.rpc.payload.timer.TimerPayload;

public sealed interface RpcPayload permits ChannelEventSubscriptionPayload, ChannelConfigPayload, TimerPayload {
}
