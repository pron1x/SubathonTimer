package tools.subathon.timer.dataservice.service.exception;

public class MissingChannelConfigurationException extends Exception {
    private final String channelId;

    public MissingChannelConfigurationException(String channelId) {
        super(String.format("Missing channel configuration for channel with ID: %s.", channelId));
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }
}
