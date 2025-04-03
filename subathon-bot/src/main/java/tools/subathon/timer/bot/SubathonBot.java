package tools.subathon.timer.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import tools.subathon.timer.bot.service.RabbitMessageService;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.enums.Command;
import tools.subathon.timer.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class SubathonBot implements HasLogger {

    private static final String EVENT_SOURCE = "subathon-bot";

    @Value("${bot.subathon.channels}")
    private List<String> channelNames;

    @Value("${bot.subathon.command.prefix}")
    private String COMMAND_PREFIX;

    private final ObjectMapper objectMapper;

    private final RabbitMessageService messageService;

    private final TwitchClient twitchClient;

    @Autowired
    public SubathonBot(ObjectMapper objectMapper, RabbitMessageService messageService, TwitchClient twitchClient) {
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.twitchClient = twitchClient;
    }

    @PostConstruct
    public void init() {
        getLogger().info("Joining {} channels.", channelNames.size());
        for(String channel : channelNames) {
            twitchClient.getChat().joinChannel(channel);
            getLogger().info("Joined channel '{}'.", channel);
        }

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            getLogger().trace("Received message. [{}: {}]", event.getUser().getName(), event.getMessage());
            if (event.getMessage().startsWith(COMMAND_PREFIX)) {
                String[] split = event.getMessage().trim().split(" ");
                String command = split[0].substring(1);

                // !timer command needs at least a subcommand, subcommand args will be checked for the command
                if ("timer".equals(command) && split.length > 1 && hasPermission(event.getPermissions())) {
                    String[] args = Arrays.stream(split).skip(2).toArray(String[]::new);
                    handleCommand(split[1], event.getChannel(), event.getUser(), args);
                }
            }
        });
    }

    private void handleCommand(String command, EventChannel eventChannel, EventUser user, String... args) {
        getLogger().debug("Handling '!timer' command for channel '{} ({})'. Sub command: {}, args: {}", eventChannel.getName(), eventChannel.getId(), command, args);
        switch (command) {
            case "start" -> handleStateChangeCommand(eventChannel.getId(), user, false);
            case "pause" -> handleStateChangeCommand(eventChannel.getId(), user, true);
            case "add" -> {
                long seconds;
                try {
                    seconds = parseArgsToSeconds(args);
                } catch (IllegalArgumentException e) {
                    getLogger().info("Not executing add command due to invalid args.");
                    twitchClient.getChat().sendMessage(eventChannel.getName(), "Invalid arguments!");
                    return;
                }
                handleTimeChangeCommand(eventChannel.getId(), user, seconds, false);
                twitchClient.getChat().sendMessage(eventChannel.getName(), String.format("Queued adding %d seconds to timer.", seconds));
            }
            case "del" -> {
                long seconds;
                try {
                    seconds = parseArgsToSeconds(args);
                } catch (IllegalArgumentException e) {
                    getLogger().info("Not executing del command due to invalid args.");
                    twitchClient.getChat().sendMessage(eventChannel.getName(), "Invalid arguments!");
                    return;
                }
                handleTimeChangeCommand(eventChannel.getId(), user, seconds, true);
                twitchClient.getChat().sendMessage(eventChannel.getName(), String.format("Queued removing %d seconds from timer.", seconds));
            }
        }
    }

    private void handleStateChangeCommand(String channelId, EventUser user, boolean isPause) {
        getLogger().debug("Handling timer state change command [{}]", isPause ? "pause" : "start");

        SubathonCommandEvent event = isPause ? createCommandEvent(user.getName(), Command.PAUSE) :
                createCommandEvent(user.getName(), Command.START);
        SubathonEventMessage message = new SubathonEventMessage();
        message.setChannelId(channelId);
        message.setSubathonEvent(event);
        try {
            messageService.sendMessage(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            getLogger().error("Failed to serialize subathon command event!", e);
        }
    }

    private void handleTimeChangeCommand(String channelId, EventUser user, long seconds, boolean isRemove) {
        getLogger().debug("Handling timer time change command [{}]", isRemove ? "del" : "add");
        SubathonCommandEvent event = isRemove ? createCommandEvent(user.getName(), Command.REMOVE, seconds) :
                createCommandEvent(user.getName(), Command.ADD, seconds);
        SubathonEventMessage message = new SubathonEventMessage();
        message.setChannelId(channelId);
        message.setSubathonEvent(event);
        try {
            messageService.sendMessage(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            getLogger().error("Failed to serialize subathon command event!", e);
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

    private boolean hasPermission(Set<CommandPermission> permissions) {
        return permissions.stream().anyMatch(p -> p == CommandPermission.OWNER || p == CommandPermission.MODERATOR);
    }

}
