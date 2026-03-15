package tools.subathon.timer.bot.handlers;

import com.github.twitch4j.eventsub.domain.chat.Badge;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.timer.bot.command.CommandUtils;
import tools.subathon.timer.bot.service.DataserviceRpcService;
import tools.subathon.timer.bot.service.TwitchChatService;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.enums.Command;
import tools.subathon.timer.datamodel.enums.TimerState;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.Arrays;
import java.util.List;

@Component
public class ChannelChatMessageEventHandler implements HasLogger {

    private final static String TIMER_COMMAND = "timer";
    private final static String COMMAND_START = "start";
    private final static String COMMAND_PAUSE = "pause";
    private final static String COMMAND_ADD = "add";
    private final static String COMMAND_DEL = "del";

    private final static List<String> TIMER_COMMANDS = List.of(COMMAND_START, COMMAND_PAUSE, COMMAND_ADD, COMMAND_DEL);

    @Value("${bot.subathon.command.prefix}")
    private String commandPrefix;

    private final TwitchChatService twitchChatService;
    private final DataserviceRpcService dataserviceRpcService;

    @Autowired
    public ChannelChatMessageEventHandler(TwitchChatService twitchChatService, DataserviceRpcService dataserviceRpcService) {
        this.twitchChatService = twitchChatService;
        this.dataserviceRpcService = dataserviceRpcService;
    }

    public void handle(ChannelChatMessageEvent event) {
        getLogger().trace("Received channel message from {} in channel {}. Message: {}.", event.getChatterUserName(), event.getBroadcasterUserName(), event.getMessage().getCleanedText());
        if (isCommandMessage(event) && !twitchChatService.isChatterOwnBot(event.getChatterUserId()) && hasPermission(event.getBadges())) {
            processCommand(event);
        }
    }

    private void processCommand(ChannelChatMessageEvent event) {
        String[] split = event.getMessage().getCleanedText().trim().split(" ");

        if (split.length < 2 || !TIMER_COMMANDS.contains(split[1])) {
            twitchChatService.sendChatMessage(event.getBroadcasterUserId(), "NotLikeThis I don't know that command. Available options: " + String.join(", ", TIMER_COMMANDS));
            return;
        }

        String[] args = Arrays.stream(split).skip(2).toArray(String[]::new);
        handleCommand(split[1], event.getBroadcasterUserId(), event.getChatterUserName(), args);
    }

    private void handleCommand(String subcommand, String broadcasterUserId, String userName, String... args) {
        getLogger().debug("Handling '!timer' command for channel '{}'. Sub command: {}, args: {}", broadcasterUserId, subcommand, args);
        switch(subcommand) {
            case COMMAND_START -> handleStartCommand(broadcasterUserId, userName);
            case COMMAND_PAUSE -> handlePauseCommand(broadcasterUserId, userName);
            case COMMAND_ADD -> handleTimeAddCommand(broadcasterUserId, userName, args);
            case COMMAND_DEL -> handleTimeRemoveCommand(broadcasterUserId, userName, args);
        }
    }

    private void handleStartCommand(String broadcasterUserId, String userName) {
        if (executeStateChange(broadcasterUserId, CommandUtils.createCommandEvent(userName, Command.START), TimerState.TICKING)) {
            twitchChatService.sendChatMessage(broadcasterUserId, "Timer started.");
        } else {
            twitchChatService.sendChatMessage(broadcasterUserId, "Failed to start the timer. Please try again...");
        }
    }

    private void handlePauseCommand(String broadcasterUserId, String userName) {
        if (executeStateChange(broadcasterUserId, CommandUtils.createCommandEvent(userName, Command.PAUSE), TimerState.PAUSED)) {
            twitchChatService.sendChatMessage(broadcasterUserId, "Timer paused.");
        } else {
            twitchChatService.sendChatMessage(broadcasterUserId, "Failed to pause the timer. Please try again...");
        }
    }

    private void handleTimeAddCommand(String broadcasterUserId, String userName, String[] args) {
        long seconds;
        try {
            seconds = CommandUtils.parseArgsToSeconds(args);
        } catch (IllegalArgumentException e) {
            getLogger().info("Failed to parse args to seconds: {}", e.getMessage());
            twitchChatService.sendChatMessage(broadcasterUserId, "Invalid arguments!");
            return;
        }
        if (executeCommand(broadcasterUserId, CommandUtils.createCommandEvent(userName, Command.ADD, seconds))) {
            twitchChatService.sendChatMessage(broadcasterUserId, "Added " + seconds + " seconds to the timer.");
        } else {
            twitchChatService.sendChatMessage(broadcasterUserId, "Could not add time, please try again...");
        }
    }

    private void handleTimeRemoveCommand(String broadcasterUserId, String userName, String[] args) {
        long seconds;
        try {
            seconds = CommandUtils.parseArgsToSeconds(args);
        } catch (IllegalArgumentException e) {
            getLogger().info("Failed to parse args to seconds: {}", e.getMessage());
            twitchChatService.sendChatMessage(broadcasterUserId, "Invalid arguments!");
            return;
        }
        if (executeCommand(broadcasterUserId, CommandUtils.createCommandEvent(userName, Command.REMOVE, seconds))) {
            twitchChatService.sendChatMessage(broadcasterUserId, "Removed " + seconds + " seconds from the timer.");
        } else {
            twitchChatService.sendChatMessage(broadcasterUserId, "Could not remove time, please try again...");
        }
    }

    private boolean executeStateChange(String broadcasterUserId, SubathonCommandEvent event, TimerState expectedState) {
        getLogger().debug("Handling timer state change command [{}]", event.getCommand());
        try {
            RpcResponse<TimerDto> response = dataserviceRpcService.executeBotCommand(broadcasterUserId, event);
            return switch (response) {
                case RpcResponse.Success<TimerDto> success -> success.body().state() == expectedState;
                case RpcResponse.Failure<TimerDto> error -> {
                    getLogger().error("Error while executing state change command for channelId {}: {}", broadcasterUserId, error.errorMessage());
                    yield false;
                }
            };
        } catch (Exception e) {
            getLogger().error("Failed to send and receive state change command!", e);
            return false;
        }
    }

    private boolean executeCommand(String broadcasterUserId, SubathonCommandEvent event) {
        getLogger().debug("Handling timer time change command [{}]", event.getCommand());
        try {
            RpcResponse<TimerDto> response = dataserviceRpcService.executeBotCommand(broadcasterUserId, event);
            return switch (response) {
                case RpcResponse.Success<TimerDto> success -> true;
                case RpcResponse.Failure<TimerDto> error -> {
                    getLogger().warn("Error while executing time change command for channelId {}: {}", broadcasterUserId, error.errorMessage());
                    yield false;
                }
            };
        } catch (Exception e) {
            getLogger().error("Failed to send and receive time change command!", e);
            return false;
        }
    }

    private boolean isCommandMessage(ChannelChatMessageEvent event) {
        return  event.getMessage().getCleanedText().startsWith(commandPrefix + TIMER_COMMAND);
    }

    private boolean hasPermission(List<Badge> badges) {
        return badges.stream().anyMatch(p -> "moderator".equals(p.getSetId()) || "broadcaster".equals(p.getSetId()));
    }

}
