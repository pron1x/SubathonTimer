package tools.subathon.timer.bot.util;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Arrays;

public class CommandUtils {

    private CommandUtils() {
        // Hide constructor
    }

    public static long parseArgsToSeconds(@NonNull String[] args) throws IllegalArgumentException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing argument to parse!");
        }
        long seconds = 0;
        for (String s : args) {
            String iso = "PT" + s.toUpperCase();
            try {
                Duration d = Duration.parse(iso);
                seconds += d.getSeconds();
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to parse arguments to duration! Args: " + Arrays.toString(args), e);
            }
        }
        return seconds;
    }

}
