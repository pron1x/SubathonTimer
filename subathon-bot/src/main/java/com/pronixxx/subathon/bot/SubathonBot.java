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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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
            case "start" -> handleStartCommand(user);
            case "pause" -> getLogger().debug("Pausing timer.");
            case "add" -> getLogger().debug("Adding time to timer: {}", Arrays.toString(args));
            case "del" -> getLogger().debug("Removing time from timer: {}", Arrays.toString(args));
        }
    }

    private void handleStartCommand(EventUser user) {
        getLogger().info("Starting the timer!");
        SubathonCommandEvent event = createCommandEvent(user.getName(), Command.START);
        try {
            messageService.sendMessage(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            getLogger().error("Failed to serialize subathon command event!", e);
        }
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
        event.setTimestamp(LocalDateTime.now(ZoneId.of(GlobalDefinition.TZ)));
        return event;
    }

    private boolean hasPermission(Set<CommandPermission> permissions) {
        return permissions.stream().anyMatch(p -> p == CommandPermission.OWNER || p == CommandPermission.MODERATOR);
    }

}
