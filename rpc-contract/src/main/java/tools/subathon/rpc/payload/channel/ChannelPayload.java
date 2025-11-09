package tools.subathon.rpc.payload.channel;

import tools.subathon.rpc.payload.RpcPayload;

public sealed interface ChannelPayload extends RpcPayload permits JoinChannelPayload {
}
