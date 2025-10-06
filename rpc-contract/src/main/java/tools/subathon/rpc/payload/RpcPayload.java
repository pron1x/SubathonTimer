package tools.subathon.rpc.payload;

import tools.subathon.rpc.payload.channel.ChannelPayload;
import tools.subathon.rpc.payload.configuration.ChannelConfigPayload;
import tools.subathon.rpc.payload.streamelements.StreamelementsPayload;
import tools.subathon.rpc.payload.timer.TimerPayload;

public sealed interface RpcPayload permits ChannelPayload, ChannelConfigPayload, StreamelementsPayload, TimerPayload {
}
