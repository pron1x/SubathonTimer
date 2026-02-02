package tools.subathon.timer.dataservice.data.domain.exception;

import tools.subathon.timer.dataservice.data.domain.enums.TimerState;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

public class IllegalTimerStateException extends RuntimeException {

    private final Collection<TimerState> expected;
    private final TimerState actual;
    private final String action;

    public IllegalTimerStateException(TimerState expected, TimerState actual, String action) {
        this(List.of(expected), actual, action);
    }

    public IllegalTimerStateException(Collection<TimerState> expected, TimerState actual, String action) {
        super(buildMessage(expected, actual, action));
        this.expected = expected;
        this.actual = actual;
        this.action = action;
    }

    public Collection<TimerState> getExpected() {
        return expected;
    }

    public TimerState getActual() {
        return actual;
    }

    public String getAction() {
        return action;
    }

    private static String buildMessage(Collection<TimerState> expected, TimerState actual, String action) {
        StringJoiner joiner = new StringJoiner(", ");
        expected.forEach(t -> joiner.add(t.name()));
        return "Cannot " + action + " timer. Expected state(s): [" + joiner + "], but was: " + (actual != null ? actual.name() : " null");
    }
}
