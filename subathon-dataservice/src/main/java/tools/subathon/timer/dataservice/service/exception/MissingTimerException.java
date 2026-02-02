package tools.subathon.timer.dataservice.service.exception;

public class MissingTimerException extends Exception {

    private final String channelId;
    private final String action;

    public MissingTimerException(String channelId, String action) {
        super(String.format("Missing timer for channel with ID: %s. Cannot %s it.", channelId, action));
        this.channelId = channelId;
        this.action = action;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getAction() {
        return action;
    }
}
