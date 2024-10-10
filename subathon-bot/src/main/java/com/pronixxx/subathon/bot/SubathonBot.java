package com.pronixxx.subathon.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventUser;
import com.pronixxx.subathon.bot.service.RabbitMessageService;
import com.pronixxx.subathon.datamodel.SubathonCommandEvent;
import com.pronixxx.subathon.datamodel.enums.Command;
import com.pronixxx.subathon.util.GlobalDefinition;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Set;

@Component
public class SubathonBot implements HasLogger {

    private static final String EVENT_SOURCE = "subathon-bot";

    @Value("${bot.subathon.channel}")
    private String CHANNEL_NAME;

    @Value("${bot.subathon.command.prefix}")
    private String COMMAND_PREFIX;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RabbitMessageService messageService;

    @Autowired
    TwitchClient twitchClient;

    @PostConstruct
    public void init() {
        twitchClient.getChat().joinChannel(CHANNEL_NAME);

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            getLogger().trace("Received message. [{}: {}]", event.getUser().getName(), event.getMessage());
            if (event.getMessage().startsWith(COMMAND_PREFIX)) {
                String[] split = event.getMessage().trim().split(" ");
                String command = split[0].substring(1);

                // !timer command needs at least a subcommand, subcommand args will be checked for the command
                if ("timer".equals(command) && split.length > 1 && hasPermission(event.getPermissions())) {
                    String[] args = Arrays.stream(split).skip(2).toArray(String[]::new);
                    handleCommand(split[1], event.getUser(), args);
                }
            }
        });
    }

    private void handleCommand(String command, EventUser user, String... args) {
        getLogger().debug("Handling '!timer' command. Sub command: {}, args: {}", command, args);
        switch (command) {
            case "start" -> handleStateChangeCommand(user, false);
            case "pause" -> handleStateChangeCommand(user, true);
            case "add" -> {
                long seconds;
                try {
                    seconds = parseArgsToSeconds(args);
                } catch (IllegalArgumentException e) {
                    getLogger().info("Not executing add command due to invalid args.");
                    twitchClient.getChat().sendMessage(CHANNEL_NAME, "Invalid arguments!");
                    return;
                }
                handleTimeChangeCommand(user, seconds, false);
                twitchClient.getChat().sendMessage(CHANNEL_NAME, String.format("Queued adding %d seconds to timer.", seconds));
            }
            case "del" -> {
                long seconds;
                try {
                    seconds = parseArgsToSeconds(args);
                } catch (IllegalArgumentException e) {
                    getLogger().info("Not executing del command due to invalid args.");
                    twitchClient.getChat().sendMessage(CHANNEL_NAME, "Invalid arguments!");
                    return;
                }
                handleTimeChangeCommand(user, seconds, true);
                twitchClient.getChat().sendMessage(CHANNEL_NAME, String.format("Queued removing %d seconds from timer.", seconds));
            }
        }
    }

    private void handleStateChangeCommand(EventUser user, boolean isPause) {
        getLogger().debug("Handling timer state change command [{}]", isPause ? "pause" : "start");
        SubathonCommandEvent event = isPause ? createCommandEvent(user.getName(), Command.PAUSE) :
                createCommandEvent(user.getName(), Command.START);
        try {
            messageService.sendMessage(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            getLogger().error("Failed to serialize subathon command event!", e);
        }
    }

    private void handleTimeChangeCommand(EventUser user, long seconds, boolean isRemove) {
        getLogger().debug("Handling timer time change command [{}]", isRemove ? "del" : "add");
        SubathonCommandEvent event = isRemove ? createCommandEvent(user.getName(), Command.REMOVE, seconds) :
                createCommandEvent(user.getName(), Command.ADD, seconds);
        try {
            messageService.sendMessage(objectMapper.writeValueAsString(event));
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
