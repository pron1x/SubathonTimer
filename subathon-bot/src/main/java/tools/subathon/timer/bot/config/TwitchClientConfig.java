package tools.subathon.timer.bot.config;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.common.util.ThreadUtils;
import com.github.twitch4j.eventsub.socket.IEventSubConduit;
import com.github.twitch4j.eventsub.socket.conduit.TwitchConduitSocketPool;
import com.github.twitch4j.eventsub.socket.conduit.exceptions.ConduitNotFoundException;
import com.github.twitch4j.eventsub.socket.conduit.exceptions.ConduitResizeException;
import com.github.twitch4j.eventsub.socket.conduit.exceptions.CreateConduitException;
import com.github.twitch4j.eventsub.socket.conduit.exceptions.ShardRegistrationException;
import com.github.twitch4j.eventsub.socket.conduit.exceptions.ShardTimeoutException;
import com.github.twitch4j.helix.domain.ConduitList;
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
                .build();
    }

    @Bean
    TwitchIdentityProvider twitchIdentityProvider() {
        return new TwitchIdentityProvider(clientId, clientSecret, null);
    }

    @Bean
    IEventSubConduit eventSubConduit(TwitchClient twitchClient) throws ShardTimeoutException, ConduitResizeException, CreateConduitException, ConduitNotFoundException, ShardRegistrationException {
        ConduitList conduitInfo = twitchClient.getHelix().getConduits(null).execute();

        return TwitchConduitSocketPool.create(spec -> {
            if (!conduitInfo.getConduits().isEmpty()) {
                spec.conduitId(conduitInfo.getConduits().getFirst().getId());
            }
            spec.clientId(clientId);
            spec.clientSecret(clientSecret);
            spec.poolShards(2);
        });
    }
}
