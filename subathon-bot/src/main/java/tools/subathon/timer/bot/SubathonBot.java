package tools.subathon.timer.bot;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.EventSubSubscriptionStatus;
import com.github.twitch4j.eventsub.condition.ChannelChatCondition;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.socket.IEventSubConduit;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.helix.domain.EventSubSubscriptionList;
import tools.subathon.timer.bot.handlers.ChannelChatMessageEventHandler;
import tools.subathon.timer.bot.handlers.ChannelFollowEventHandler;
import tools.subathon.timer.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SubathonBot implements HasLogger {

    @Value("${bot.twitch.user.id}")
    private String botId;

    private final TwitchClient twitchClient;

    private final IEventSubConduit conduit;

    private final ChannelChatMessageEventHandler chatMessageEventHandler;
    private final ChannelFollowEventHandler followEventHandler;

    @Autowired
    public SubathonBot(TwitchClient twitchClient, IEventSubConduit conduit, ChannelChatMessageEventHandler chatMessageEventHandler, ChannelFollowEventHandler followEventHandler) {
        this.chatMessageEventHandler = chatMessageEventHandler;
        this.twitchClient = twitchClient;
        this.conduit = conduit;
        this.followEventHandler = followEventHandler;
    }

    @PostConstruct
    public void init() {
        getLogger().info("Setting up conduit subscriptions.");

        subscribeToChannelMessages("91658662");
        subscribeToFollowEvents("91658662");

        conduit.getEventManager().onEvent(ChannelChatMessageEvent.class, chatMessageEventHandler::handle);
        conduit.getEventManager().onEvent(ChannelFollowEvent.class, followEventHandler::handle);
    }

    public boolean subscribeToChannelMessages(String broadcasterUserId) {
        getLogger().debug("Creating chat message subscription for broadcaster user id '{}'.", broadcasterUserId);

        Optional<EventSubSubscription> subscription = conduit.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE,
                b -> b.broadcasterUserId(broadcasterUserId).userId(botId).build());
        return subscription.isPresent();

    }

    public List<String> getMessageSubscriptionChannelIds() {
        EventSubSubscriptionList subscriptions = twitchClient.getHelix().getEventSubSubscriptions(null, null,SubscriptionTypes.CHANNEL_CHAT_MESSAGE, null, null, null).execute();
        return subscriptions.getSubscriptions().stream()
                .filter(s -> s.getStatus().equals(EventSubSubscriptionStatus.ENABLED))
                .map(EventSubSubscription::getCondition)
                .map(c -> (ChannelChatCondition) c)
                .map(ChannelChatCondition::getBroadcasterUserId).toList();
    }

    public boolean subscribeToFollowEvents(String broadcasterUserId) {
        getLogger().debug("Creating follow event subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = conduit.register(SubscriptionTypes.CHANNEL_FOLLOW_V2,
                b -> b.broadcasterUserId(broadcasterUserId).moderatorUserId(broadcasterUserId).build());
        return subscription.isPresent();
    }

}
