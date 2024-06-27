package com.pronixxx.subathon.bot.config;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwitchClientConfig {

    @Value("${bot.twitch.oauth.token}")
    private String oauth2Token;

    @Bean
    TwitchClient twitchClient() {
        OAuth2Credential credential = new OAuth2Credential("twitch", oauth2Token);
        return TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withChatAccount(credential)
                .build();
    }
}
