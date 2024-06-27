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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SubathonBot implements HasLogger {

    @Value("${bot.subathon.channel}")
    private String CHANNEL_NAME;

    @Value("${bot.subathon.command.prefix}")
    private String COMMAND_PREFIX;

    private Map<BotCommand, List<CommandPermission>> permissionsMap;

    @Autowired
    RabbitMessageService messageService;

    @Autowired
    TwitchClient twitchClient;

    @PostConstruct
    public void init() {
        permissionsMap = new HashMap<>();
        permissionsMap.put(BotCommand.TIMER_START, List.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

        twitchClient.getChat().joinChannel(CHANNEL_NAME);

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            getLogger().trace("Received message. [{}: {}]", event.getUser().getName(), event.getMessage());
            if(event.getMessage().startsWith(COMMAND_PREFIX)) {
                handleCommand(event);
            }
        });
    }

    private void handleCommand(ChannelMessageEvent event) {
        getLogger().debug("Received command [{}: {}]", event.getUser().getName(), event.getMessage());
        String[] args = event.getMessage().trim().split(" ");
        String command = args[0].substring(1);
        switch (command) {
            case "timerstart" -> handleStartCommand(event.getPermissions(), event.getUser());
            case "timerpause" -> getLogger().info("Pausing timer");
            case "timerresume" -> getLogger().info("Resuming timer");
            case "timeradd" -> getLogger().info("Adding time");
            case "timerremove" -> getLogger().info("Removing time");
            default -> getLogger().info("Unknown command '{}'!", command);
        }
    }

    private void handleStartCommand(Set<CommandPermission> permissions, EventUser user) {
        if(hasPermission(permissions, BotCommand.TIMER_START)) {
            getLogger().info("Starting the timer!");
        }
    }

    private boolean hasPermission(Set<CommandPermission> permissions, BotCommand command) {
        return permissions.stream().anyMatch(permission -> permissionsMap.get(command).contains(permission));
    }

    enum BotCommand {
        TIMER_START
    }

}
