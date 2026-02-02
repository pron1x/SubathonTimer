package tools.subathon.timer.dataservice.data.domain.exception;

import java.time.Duration;

public class InsufficientTimeRemainingException extends RuntimeException {

    public InsufficientTimeRemainingException(Duration attemptedToDeduct, Duration timeRemaining) {
        super(String.format("Attempted to deduct %s seconds, but only %s seconds remain.", attemptedToDeduct.toSeconds(), timeRemaining.toSeconds()));
    }
}
