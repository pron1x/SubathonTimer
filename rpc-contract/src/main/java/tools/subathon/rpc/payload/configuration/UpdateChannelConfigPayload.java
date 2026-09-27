package tools.subathon.rpc.payload.configuration;

@Deprecated
public record UpdateChannelConfigPayload(String channelId, Object channelConfig) implements ChannelConfigPayload {

}
