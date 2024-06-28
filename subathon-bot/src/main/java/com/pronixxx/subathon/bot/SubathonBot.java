package com.pronixxx.subathon.bot;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventUser;
import com.pronixxx.subathon.bot.service.RabbitMessageService;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SubathonBot implements HasLogger {

    @Value("${bot.subathon.channel}")
    private String CHANNEL_NAME;

    @Value("${bot.subathon.command.prefix}")
    private String COMMAND_PREFIX;


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
    }

    private boolean hasPermission(Set<CommandPermission> permissions) {
        return permissions.stream().anyMatch(p -> p == CommandPermission.OWNER || p == CommandPermission.MODERATOR);
    }

}
