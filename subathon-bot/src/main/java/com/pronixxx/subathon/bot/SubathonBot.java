package com.pronixxx.subathon.bot;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.pronixxx.subathon.bot.service.RabbitMessageService;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
            case "timerpause" -> getLogger().info("Pausing timer");
            case "timerresume" -> getLogger().info("Resuming timer");
            case "timeradd" -> getLogger().info("Adding time");
            case "timerremove" -> getLogger().info("Removing time");
            default -> getLogger().info("Unknown command '{}'!", command);
        }
    }

}
