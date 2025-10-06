package tools.subathon.rpc.payload.configuration;

public record UpdateChannelConfigPayload(String channelId, Object channelConfig) implements ChannelConfigPayload {

}
