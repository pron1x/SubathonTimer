package tools.subathon.timer.bot.config;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.common.util.ThreadUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class TwitchClientConfig implements HasLogger {

    @Value("${bot.twitch.oauth.client-id}")
    private String clientId;

    @Value("${bot.twitch.oauth.client-secret}")
    private String clientSecret;

    @Bean
    TwitchClient twitchClient(TwitchIdentityProvider twitchIdentityProvider) {
        OAuth2Credential appCredential = twitchIdentityProvider.getAppAccessToken();

        // Refresh the App Token daily
        ScheduledThreadPoolExecutor executor = ThreadUtils.getDefaultScheduledThreadPoolExecutor("twitch4j", Runtime.getRuntime().availableProcessors());
        executor.scheduleAtFixedRate(
                () -> appCredential.updateCredential(twitchIdentityProvider.getAppAccessToken()),
                1L, 1L, TimeUnit.DAYS
        );

        return TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withDefaultAuthToken(appCredential)
                .withScheduledThreadPoolExecutor(executor)
                .withEnableChat(true)
                .build();
    }

    @Bean
    TwitchIdentityProvider twitchIdentityProvider() {
        return new TwitchIdentityProvider(clientId, clientSecret, null);
    }
}
