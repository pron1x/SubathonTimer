package com.pronixxx.subathon.bot;

import com.github.philippheuer.events4j.api.domain.IDisposable;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.pronixxx.subathon.bot.service.RabbitMessageService;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubathonBot implements HasLogger {

    @Autowired
    RabbitMessageService messageService;

    @Autowired
    TwitchClient twitchClient;

    @PostConstruct
    public void init() {
        twitchClient.getChat().joinChannel("pronixxx");
        IDisposable handlerReg = twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            getLogger().info("Received message event! [{}]{}:{}", event.getPermissions().toString(), event.getUser().getName(), event.getMessage());
        });
    }
}
