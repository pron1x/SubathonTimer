package tools.subathon.rpc.payload.configuration;

import tools.subathon.rpc.payload.RpcPayload;

public sealed interface ChannelConfigPayload extends RpcPayload permits GetChannelConfigPayload, UpdateChannelConfigPayload {

}
