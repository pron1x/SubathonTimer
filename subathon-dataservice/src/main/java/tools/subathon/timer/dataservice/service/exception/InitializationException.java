package tools.subathon.timer.dataservice.service.exception;

public class InitializationException extends Exception {
    private final String channelId;
    private final String initStep;

    public InitializationException(String channelId, String initStep) {
        super(String.format("Failed to initialize timer for channel with ID: %s. Failed init step: %s.", channelId, initStep));
        this.channelId = channelId;
        this.initStep = initStep;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getInitStep() {
        return initStep;
    }
}
