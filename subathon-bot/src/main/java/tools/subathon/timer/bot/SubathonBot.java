package tools.subathon.timer.bot;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.EventSubSubscriptionStatus;
import com.github.twitch4j.eventsub.condition.ChannelChatCondition;
import com.github.twitch4j.eventsub.domain.chat.Badge;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.socket.IEventSubConduit;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.helix.domain.ChatMessage;
import com.github.twitch4j.helix.domain.EventSubSubscriptionList;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.timer.bot.service.DataserviceRpcService;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.enums.Command;
import tools.subathon.timer.datamodel.enums.TimerState;
import tools.subathon.timer.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class SubathonBot implements HasLogger {

    private static final String EVENT_SOURCE = "TwitchChat";

    @Value("${bot.twitch.user.id}")
    private String botId;

    @Value("${bot.subathon.command.prefix}")
    private String COMMAND_PREFIX;

    private final DataserviceRpcService dataserviceRpcService;

    private final TwitchClient twitchClient;

    private final IEventSubConduit conduit;

    @Autowired
    public SubathonBot(DataserviceRpcService dataserviceRpcService, TwitchClient twitchClient, IEventSubConduit conduit) {
        this.dataserviceRpcService = dataserviceRpcService;
        this.twitchClient = twitchClient;
        this.conduit = conduit;
    }

    @PostConstruct
    public void init() {
        getLogger().info("Setting up conduit subscriptions.");

        subscribeToChannelMessages("91658662");

        conduit.getEventManager().onEvent(ChannelChatMessageEvent.class, event -> {
            getLogger().debug("Received channel message from {} in channel {}. Message: {}.", event.getChatterUserName(), event.getBroadcasterUserName(), event.getMessage().getCleanedText());
            if (event.getMessage().getCleanedText().startsWith(COMMAND_PREFIX) && !botId.equals(event.getChatterUserId())) {
                String[] split = event.getMessage().getCleanedText().trim().split(" ");
                String command = split[0].substring(1);

                // !timer command needs at least a subcommand, subcommand args will be checked for the command
                if ("timer".equals(command) && split.length > 1 && hasPermission(event.getBadges())) {
                    String[] args = Arrays.stream(split).skip(2).toArray(String[]::new);
                    handleCommand(split[1], event.getBroadcasterUserId(), event.getChatterUserName(), args);
                }
            }
        });
    }

    public boolean subscribeToChannelMessages(String channelId) {
        getLogger().info("Creating chat message subscription for channel '{}'.", channelId);

        if (!getMessageSubscriptionChannelIds().contains(channelId)) {
            Optional<EventSubSubscription> subscription = conduit.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE, b -> b.broadcasterUserId(channelId).userId(botId).build());
            return subscription.isPresent();
        } else {
            getLogger().info("Subscription for channel '{}' already existed.", channelId);
            return true;
        }
    }

    public List<String> getMessageSubscriptionChannelIds() {
        EventSubSubscriptionList subscriptions = twitchClient.getHelix().getEventSubSubscriptions(null, null,SubscriptionTypes.CHANNEL_CHAT_MESSAGE, null, null, null).execute();
        return subscriptions.getSubscriptions().stream().filter(s -> s.getStatus().equals(EventSubSubscriptionStatus.ENABLED)).map(EventSubSubscription::getCondition).map(c -> (ChannelChatCondition) c).map(ChannelChatCondition::getBroadcasterUserId).toList();
    }

    private void handleCommand(String command, String eventChannelId, String userName, String... args) {
        getLogger().debug("Handling '!timer' command for channel '{}'. Sub command: {}, args: {}", eventChannelId, command, args);
        switch (command) {
            case "start" -> {
                if(handleStateChangeCommand(eventChannelId, userName, false)) {
                    sendMessageWithHelix(eventChannelId, "Timer started.");
                } else {
                    sendMessageWithHelix(eventChannelId, "Failed to start the timer. Please try again...");
                }
            }
            case "pause" -> {
                if(handleStateChangeCommand(eventChannelId, userName, true)) {
                    sendMessageWithHelix(eventChannelId, "Timer paused.");
                } else {
                    sendMessageWithHelix(eventChannelId, "Failed to pause the timer. Please try again...");
                }
            }
            case "add" -> {
                long seconds;
                try {
                    seconds = parseArgsToSeconds(args);
                } catch (IllegalArgumentException e) {
                    getLogger().info("Not executing add command due to invalid args.");
                    sendMessageWithHelix(eventChannelId, "Invalid arguments!");
                    return;
                }
                if(handleTimeChangeCommand(eventChannelId, userName, seconds, false)) {
                    sendMessageWithHelix(eventChannelId, String.format("Added %d seconds to the timer.", seconds));
                } else {
                    sendMessageWithHelix(eventChannelId, "Command failed! Please try again...");
                }
            }
            case "del" -> {
                long seconds;
                try {
                    seconds = parseArgsToSeconds(args);
                } catch (IllegalArgumentException e) {
                    getLogger().info("Not executing del command due to invalid args.");
                    sendMessageWithHelix(eventChannelId, "Invalid arguments!");
                    return;
                }
                if(handleTimeChangeCommand(eventChannelId, userName, seconds, true)) {
                    sendMessageWithHelix(eventChannelId, String.format("Removed %d seconds from the timer.", seconds));
                } else {
                    sendMessageWithHelix(eventChannelId, "Command failed! Please try again...");
                }
            }
            default -> sendMessageWithHelix(eventChannelId, "I can't do that... NotLikeThis");
        }
    }

    private boolean handleStateChangeCommand(String channelId, String userName, boolean isPause) {
        getLogger().debug("Handling timer state change command [{}]", isPause ? "pause" : "start");

        SubathonCommandEvent event = isPause ? createCommandEvent(userName, Command.PAUSE) :
                createCommandEvent(userName, Command.START);
        try {
            RpcResponse<TimerDto> response = dataserviceRpcService.executeBotCommand(channelId, event);
            switch (response) {
                case RpcResponse.Success<TimerDto> success -> {
                    return success.body().state() == (isPause ? TimerState.PAUSED : TimerState.TICKING);
                }
                case RpcResponse.Failure<TimerDto> error -> {
                    getLogger().error("Error while executing state change command for channelId {}: {}", channelId, error.errorMessage());
                    return false;
                }
            }
        } catch (Exception e) {
            getLogger().error("Failed to send and receive state change command!", e);
            return false;
        }
    }

    private boolean handleTimeChangeCommand(String channelId, String userName, long seconds, boolean isRemove) {
        getLogger().debug("Handling timer time change command [{}]", isRemove ? "del" : "add");
        SubathonCommandEvent event = isRemove ? createCommandEvent(userName, Command.REMOVE, seconds) :
                createCommandEvent(userName, Command.ADD, seconds);
        try {
            RpcResponse<TimerDto> response = dataserviceRpcService.executeBotCommand(channelId, event);
            switch (response) {
                case RpcResponse.Success<TimerDto> success -> {
                    return true;
                }
                case RpcResponse.Failure<TimerDto> error -> {
                    getLogger().warn("Error while executing time change command for channelId {}: {}", channelId, error.errorMessage());
                    return false;
                }
            }
        } catch (Exception e) {
            getLogger().error("Failed to send and receive time change command!", e);
            return false;
        }
    }

    private long parseArgsToSeconds(String[] args) throws IllegalArgumentException {
        if(args.length < 1) {
            throw new IllegalArgumentException("Missing argument to parse!");
        }
        long seconds = 0;
        for(String s : args) {
            String iso = "PT" + s.toUpperCase();
            try {
                Duration d = Duration.parse(iso);
                seconds += d.getSeconds();
            } catch (Exception e) {
                getLogger().warn("Unable to parse arguments for time change command! Args: {}", Arrays.toString(args));
                throw new IllegalArgumentException(e);
            }
        }
        return seconds;
    }

    private SubathonCommandEvent createCommandEvent(String user, Command command) {
        return createCommandEvent(user, command, 0);
    }

    private SubathonCommandEvent createCommandEvent(String user, Command command, long seconds) {
        SubathonCommandEvent event = new SubathonCommandEvent();
        event.setSource(EVENT_SOURCE);
        event.setUsername(user);
        event.setCommand(command);
        event.setSeconds(seconds);
        event.setTimestamp(Instant.now());
        return event;
    }

    private boolean hasPermission(List<Badge> badges) {
        return badges.stream().anyMatch(p -> "moderator".equals(p.getSetId()) || "broadcaster".equals(p.getSetId()));
    }

    private void sendMessageWithHelix(String channelId, String message) {
        ChatMessage msg = ChatMessage.builder().senderId(botId).broadcasterId(channelId).message(message).build();
        twitchClient.getHelix().sendChatMessage(null, msg).execute();
    }

}
