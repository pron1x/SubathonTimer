package tools.subathon.timer.dataservice.service.exception;

public class DuplicateTimerException extends Exception {
    private final String channelId;

    public DuplicateTimerException(String channelId) {
        super(String.format("A timer for channel with ID: %s already exists.", channelId));
        this.channelId = channelId;
    }
}
