package tools.subathon.timer.bot;

import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.socket.IEventSubConduit;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionType;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import tools.subathon.timer.bot.handlers.ChannelChatMessageEventHandler;
import tools.subathon.timer.bot.handlers.ChannelFollowEventHandler;
import tools.subathon.timer.bot.handlers.ChannelRaidEventHandler;
import tools.subathon.timer.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class SubathonBot implements HasLogger {

    @Value("${bot.twitch.user.id}")
    private String botId;

    private final IEventSubConduit conduit;

    private final HashMap<String, List<EventSubSubscription>> subscriptions = new HashMap<>();

    private final ChannelChatMessageEventHandler chatMessageEventHandler;
    private final ChannelFollowEventHandler followEventHandler;
    private final ChannelRaidEventHandler raidEventHandler;

    @Autowired
    public SubathonBot(IEventSubConduit conduit, ChannelChatMessageEventHandler chatMessageEventHandler, ChannelFollowEventHandler followEventHandler, ChannelRaidEventHandler raidEventHandler) {
        this.chatMessageEventHandler = chatMessageEventHandler;
        this.conduit = conduit;
        this.followEventHandler = followEventHandler;
        this.raidEventHandler = raidEventHandler;
    }

    @PostConstruct
    public void init() {
        getLogger().info("Setting up conduit event handlers.");
        conduit.getEventManager().onEvent(ChannelChatMessageEvent.class, chatMessageEventHandler::handle);
        conduit.getEventManager().onEvent(ChannelFollowEvent.class, followEventHandler::handle);
        conduit.getEventManager().onEvent(ChannelRaidEvent.class, raidEventHandler::handle);
    }

    public Optional<EventSubSubscription> subscribeToChannelMessages(String broadcasterUserId) {
        getLogger().debug("Creating chat message subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = getIfExists(broadcasterUserId, SubscriptionTypes.CHANNEL_CHAT_MESSAGE);
        if (subscription.isPresent()) {
            getLogger().debug("Subscription of type '{}' for broadcaster user id '{}' already exists.", subscription.get().getRawType(), broadcasterUserId);
            return subscription;
        }
        subscription = conduit.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE,
                b -> b.broadcasterUserId(broadcasterUserId).userId(botId).build());
        subscription.ifPresent(s -> subscriptions.get(broadcasterUserId).add(s));
        return subscription;
    }

    public Optional<EventSubSubscription> subscribeToFollowEvents(String broadcasterUserId) {
        getLogger().debug("Creating follow event subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = getIfExists(broadcasterUserId, SubscriptionTypes.CHANNEL_FOLLOW_V2);
        if (subscription.isPresent()) {
            getLogger().debug("Subscription of type '{}' for broadcaster user id '{}' already exists.", subscription.get().getRawType(), broadcasterUserId);
            return subscription;
        }
        subscription = conduit.register(SubscriptionTypes.CHANNEL_FOLLOW_V2,
                b -> b.broadcasterUserId(broadcasterUserId).moderatorUserId(broadcasterUserId).build());
        subscription.ifPresent(s -> subscriptions.get(broadcasterUserId).add(s));
        return subscription;
    }

    public Optional<EventSubSubscription> subscribeToRaidEvents(String broadcasterUserId) {
        getLogger().debug("Creating raid event subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = getIfExists(broadcasterUserId, SubscriptionTypes.CHANNEL_RAID);
        if (subscription.isPresent()) {
            getLogger().debug("Subscription of type '{}' for broadcaster user id '{}' already exists.", subscription.get().getRawType(), broadcasterUserId);
            return subscription;
        }
        subscription = conduit.register(SubscriptionTypes.CHANNEL_RAID,
                b -> b.toBroadcasterUserId(broadcasterUserId).build());
        subscription.ifPresent(s ->  subscriptions.get(broadcasterUserId).add(s));
        return subscription;
    }

    private Optional<EventSubSubscription> getIfExists(String broadcasterUserId, SubscriptionType<?,?,?> subscriptionType) {
        if (!subscriptions.containsKey(broadcasterUserId)) {
            subscriptions.put(broadcasterUserId, new ArrayList<>());
        }
        return subscriptions.get(broadcasterUserId).stream()
                .filter(s -> subscriptionType.equals(s.getType()))
                .findFirst();
    }

}
