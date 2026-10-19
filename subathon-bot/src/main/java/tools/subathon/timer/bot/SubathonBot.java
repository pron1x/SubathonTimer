package tools.subathon.timer.bot;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.EventSubSubscriptionStatus;
import com.github.twitch4j.eventsub.condition.ChannelChatCondition;
import com.github.twitch4j.eventsub.condition.ChannelEventSubCondition;
import com.github.twitch4j.eventsub.condition.ChannelFollowV2Condition;
import com.github.twitch4j.eventsub.condition.ChannelRaidCondition;
import com.github.twitch4j.eventsub.events.ChannelBitsUseEvent;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.socket.IEventSubConduit;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionType;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import tools.subathon.timer.bot.handlers.ChannelBitsUseEventHandler;
import tools.subathon.timer.bot.handlers.ChannelChatMessageEventHandler;
import tools.subathon.timer.bot.handlers.ChannelChatNotificationHandler;
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
    private final ChannelBitsUseEventHandler bitsEventHandler;
    private final ChannelChatNotificationHandler chatNotificationHandler;
    private final TwitchClient twitchClient;

    @Autowired
    public SubathonBot(IEventSubConduit conduit, ChannelChatMessageEventHandler chatMessageEventHandler,
                       ChannelFollowEventHandler followEventHandler, ChannelRaidEventHandler raidEventHandler,
                       ChannelBitsUseEventHandler bitsEventHandler, ChannelChatNotificationHandler chatNotificationHandler, TwitchClient twitchClient) {
        this.chatMessageEventHandler = chatMessageEventHandler;
        this.conduit = conduit;
        this.followEventHandler = followEventHandler;
        this.raidEventHandler = raidEventHandler;
        this.bitsEventHandler = bitsEventHandler;
        this.chatNotificationHandler = chatNotificationHandler;
        this.twitchClient = twitchClient;
    }

    @PostConstruct
    public void init() {
        getLogger().info("Setting up conduit event handlers.");
        conduit.getEventManager().onEvent(ChannelChatMessageEvent.class, chatMessageEventHandler::handle);
        conduit.getEventManager().onEvent(ChannelFollowEvent.class, followEventHandler::handle);
        conduit.getEventManager().onEvent(ChannelRaidEvent.class, raidEventHandler::handle);
        conduit.getEventManager().onEvent(ChannelBitsUseEvent.class, bitsEventHandler::handle);
        conduit.getEventManager().onEvent(ChannelChatNotificationEvent.class, chatNotificationHandler::handle);

        getLogger().info("Conduit ID: {}", conduit.getConduitId());
        twitchClient.getHelix().getEventSubSubscriptions(null, null, null, null, null, null).execute()
                .getSubscriptions().forEach(sub -> {
                    if (!conduit.getConduitId().equals(sub.getTransport().getConduitId())) {
                        getLogger().debug("Stale '{}' event sub subscription for old conduit '{}' found. Status: {}", sub.getType().getName(), sub.getTransport().getConduitId(), sub.getStatus());
                        return;
                    }
                    if (!sub.getStatus().equals(EventSubSubscriptionStatus.ENABLED)) {
                        getLogger().info("Inactive '{}' event sub subscription for active conduit found. Status: {}", sub.getType().getName(), sub.getStatus());
                        return;
                    }
                    String broadcasterId = switch (sub.getCondition()) {
                        case ChannelChatCondition chatCondition -> chatCondition.getBroadcasterUserId();
                        case ChannelFollowV2Condition followCondition -> followCondition.getBroadcasterUserId();
                        case ChannelRaidCondition raidCondition -> raidCondition.getToBroadcasterUserId();
                        case ChannelEventSubCondition eventSubCondition -> eventSubCondition.getBroadcasterUserId();
                        default -> {
                            getLogger().info("Unexpected condition type '{}' in existing event sub subscriptions for active conduit", sub.getCondition().getClass().getSimpleName());
                            yield null;
                        }
                    };
                    if (broadcasterId == null) {
                        return;
                    }
                    getLogger().info("Adding existing '{}' event sub subscription for broadcaster '{}' with status '{}' to cache...", sub.getType().getName(), broadcasterId, sub.getStatus());
                    if (!subscriptions.containsKey(broadcasterId)) {
                        subscriptions.put(broadcasterId, new ArrayList<>());
                    }
                    subscriptions.get(broadcasterId).add(sub);
                });
    }

    public void subscribeToDonationMessages(String broadcasterUserId, String parserTemplate, String messageUser) {
        try {
            chatMessageEventHandler.registerNewDonationMessageParser(broadcasterUserId, parserTemplate, messageUser);
        } catch (IllegalArgumentException ex) {
            getLogger().info("Failed to create donation message parser for broadcaster user id '{}'. Error: {}", broadcasterUserId, ex.getMessage());
        }
    }

    public void removeDonationMessageParser(String broadcasterUserId) {
        chatMessageEventHandler.removeDonationMessageParser(broadcasterUserId);
    }

    public void ensureSubscriptions(String broadcasterUserId) {
        subscribeToChannelMessages(broadcasterUserId);
        subscribeToFollowEvents(broadcasterUserId);
        subscribeToRaidEvents(broadcasterUserId);
        subscribeToBitsEvents(broadcasterUserId);
        subscribeToChannelChatNotificationEvents(broadcasterUserId);
    }

    public void subscribeToChannelMessages(String broadcasterUserId) {
        getLogger().debug("Creating chat message subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = getIfExists(broadcasterUserId, SubscriptionTypes.CHANNEL_CHAT_MESSAGE);
        if (subscription.isPresent()) {
            getLogger().debug("Subscription of type '{}' for broadcaster user id '{}' already exists.", subscription.get().getRawType(), broadcasterUserId);
            return;
        }
        subscription = conduit.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE,
                b -> b.broadcasterUserId(broadcasterUserId).userId(botId).build());
        subscription.ifPresent(s -> subscriptions.get(broadcasterUserId).add(s));
    }

    public void subscribeToFollowEvents(String broadcasterUserId) {
        getLogger().debug("Creating follow event subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = getIfExists(broadcasterUserId, SubscriptionTypes.CHANNEL_FOLLOW_V2);
        if (subscription.isPresent()) {
            getLogger().debug("Subscription of type '{}' for broadcaster user id '{}' already exists.", subscription.get().getRawType(), broadcasterUserId);
            return;
        }
        subscription = conduit.register(SubscriptionTypes.CHANNEL_FOLLOW_V2,
                b -> b.broadcasterUserId(broadcasterUserId).moderatorUserId(broadcasterUserId).build());
        subscription.ifPresent(s -> subscriptions.get(broadcasterUserId).add(s));
    }

    public void subscribeToRaidEvents(String broadcasterUserId) {
        getLogger().debug("Creating raid event subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = getIfExists(broadcasterUserId, SubscriptionTypes.CHANNEL_RAID);
        if (subscription.isPresent()) {
            getLogger().debug("Subscription of type '{}' for broadcaster user id '{}' already exists.", subscription.get().getRawType(), broadcasterUserId);
            return;
        }
        subscription = conduit.register(SubscriptionTypes.CHANNEL_RAID,
                b -> b.toBroadcasterUserId(broadcasterUserId).build());
        subscription.ifPresent(s ->  subscriptions.get(broadcasterUserId).add(s));
    }

    public void subscribeToBitsEvents(String broadcasterUserId) {
        getLogger().debug("Creating bit event subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = getIfExists(broadcasterUserId, SubscriptionTypes.CHANNEL_BITS_USE);
        if (subscription.isPresent()) {
            getLogger().debug("Subscription of type '{}' for broadcaster user id '{}' already exists.", subscription.get().getRawType(), broadcasterUserId);
            return;
        }
        subscription = conduit.register(SubscriptionTypes.CHANNEL_BITS_USE,
                b -> b.broadcasterUserId(broadcasterUserId).build());
        subscription.ifPresent(s -> subscriptions.get(broadcasterUserId).add(s));
    }

    public void subscribeToChannelChatNotificationEvents(String broadcasterUserId) {
        getLogger().debug("Creating channel chat notification event subscription for broadcaster user id '{}'.", broadcasterUserId);
        Optional<EventSubSubscription> subscription = getIfExists(broadcasterUserId, SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION);
        if (subscription.isPresent()) {
            getLogger().debug("Subscription of type '{}' for broadcaster user id '{}' already exists.", subscription.get().getRawType(), broadcasterUserId);
            return;
        }
        subscription = conduit.register(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION,
                b -> b.broadcasterUserId(broadcasterUserId).userId(botId).build());
        subscription.ifPresent(s -> subscriptions.get(broadcasterUserId).add(s));
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
