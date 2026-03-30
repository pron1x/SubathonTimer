package tools.subathon.timer.bot.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandUtilsTest {

    @Test
    void parseArgsToSeconds_parsesSingleArgument() {
        String oneSecond = "1s";
        String oneMinute = "1m";
        String oneHour = "1h";
        String[] singleArgSecond = new String[] {oneSecond};
        String[] singleArgMinute = new String[] {oneMinute};
        String[] singleArgHour = new String[] {oneHour};

        String[] singleArgCombo = new String[] {oneHour + oneMinute};

        long resultSecond = assertDoesNotThrow(() -> CommandUtils.parseArgsToSeconds(singleArgSecond));
        long resultMinute = assertDoesNotThrow(() -> CommandUtils.parseArgsToSeconds(singleArgMinute));
        long resultHour = assertDoesNotThrow(() -> CommandUtils.parseArgsToSeconds(singleArgHour));

        long resultCombo = assertDoesNotThrow(() -> CommandUtils.parseArgsToSeconds(singleArgCombo));

        assertEquals(1, resultSecond);
        assertEquals(60, resultMinute);
        assertEquals(3600, resultHour);

        assertEquals(3660, resultCombo);
    }

    @Test
    void parseArgsToSeconds_parsesMultipleArguments() {
        String tenSeconds = "10s";
        String oneMinute = "1m";
        String oneHour = "1h";

        long orderedResult = assertDoesNotThrow(() -> CommandUtils.parseArgsToSeconds(new String[] {oneHour, oneMinute, tenSeconds}));
        long unorderedResult = assertDoesNotThrow(() -> CommandUtils.parseArgsToSeconds(new String[] {tenSeconds, oneHour, oneMinute}));

        assertEquals(3670,  orderedResult);
        assertEquals(3670,  unorderedResult);
    }

    @Test
    void parseArgsToSeconds_failsOnLetterInput() {
        String[] letters = new String[] {"abc"};

        assertThrows(IllegalArgumentException.class, () -> CommandUtils.parseArgsToSeconds(letters));

        String[] validAndInvalid = new String[] {"10s", "abc"};
        assertThrows(IllegalArgumentException.class, () -> CommandUtils.parseArgsToSeconds(validAndInvalid));
    }

}