package tools.subathon.timer.bot.command;

import org.jspecify.annotations.NonNull;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.enums.Command;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class CommandUtils {

    private final static String EVENT_SOURCE = "twitch-chat";

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

    public static SubathonCommandEvent createCommandEvent(String user, Command command) {
        return createCommandEvent(user, command, 0);
    }

    public static SubathonCommandEvent createCommandEvent(String user, Command command, long seconds) {
        SubathonCommandEvent event = new SubathonCommandEvent();
        event.setSource(EVENT_SOURCE);
        event.setUsername(user);
        event.setCommand(command);
        event.setSeconds(seconds);
        event.setTimestamp(Instant.now());
        return event;
    }
}
