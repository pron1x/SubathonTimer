package tools.subathon.timer.bot;

import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.socket.IEventSubConduit;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import tools.subathon.timer.bot.handlers.ChannelChatMessageEventHandler;
import tools.subathon.timer.bot.handlers.ChannelFollowEventHandler;
import tools.subathon.timer.util.interfaces.HasLogger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubathonBot implements HasLogger {

    @Value("${bot.twitch.user.id}")
    private String botId;

    private final IEventSubConduit conduit;

    private final ChannelChatMessageEventHandler chatMessageEventHandler;
    private final ChannelFollowEventHandler followEventHandler;

    @Autowired
    public SubathonBot(IEventSubConduit conduit, ChannelChatMessageEventHandler chatMessageEventHandler, ChannelFollowEventHandler followEventHandler) {
        this.chatMessageEventHandler = chatMessageEventHandler;
        this.conduit = conduit;
        this.followEventHandler = followEventHandler;
    }

    @PostConstruct
    public void init() {
        getLogger().info("Setting up conduit event handlers.");
        conduit.getEventManager().onEvent(ChannelChatMessageEvent.class, chatMessageEventHandler::handle);
        conduit.getEventManager().onEvent(ChannelFollowEvent.class, followEventHandler::handle);
    }

    public Optional<EventSubSubscription> subscribeToChannelMessages(String broadcasterUserId) {
        getLogger().debug("Creating chat message subscription for broadcaster user id '{}'.", broadcasterUserId);
        return conduit.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE,
                b -> b.broadcasterUserId(broadcasterUserId).userId(botId).build());

    }

    public Optional<EventSubSubscription> subscribeToFollowEvents(String broadcasterUserId) {
        getLogger().debug("Creating follow event subscription for broadcaster user id '{}'.", broadcasterUserId);
        return conduit.register(SubscriptionTypes.CHANNEL_FOLLOW_V2,
                b -> b.broadcasterUserId(broadcasterUserId).moderatorUserId(broadcasterUserId).build());
    }

}
