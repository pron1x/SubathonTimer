package tools.subathon.rpc.payload.configuration;

import tools.subathon.rpc.payload.RpcPayload;

@Deprecated
public sealed interface ChannelConfigPayload extends RpcPayload permits GetChannelConfigPayload, UpdateChannelConfigPayload {

}
