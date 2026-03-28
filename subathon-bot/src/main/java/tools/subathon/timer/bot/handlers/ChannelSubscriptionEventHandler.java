package tools.subathon.timer.bot.handlers;

import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.timer.bot.factories.SubathonEventMessageFactory;
import tools.subathon.timer.bot.service.RabbitMessageService;
import tools.subathon.timer.util.interfaces.HasLogger;

@Service
public class ChannelSubscriptionEventHandler implements HasLogger {

    private final RabbitMessageService rabbitMessageService;

    @Autowired
    public ChannelSubscriptionEventHandler(RabbitMessageService rabbitMessageService) {
        this.rabbitMessageService = rabbitMessageService;
    }

    public void handle(ChannelSubscribeEvent event) {
        getLogger().debug("Handling ChannelSubscribeEvent for broadcaster user id '{}'", event.getBroadcasterUserId());
        rabbitMessageService.produceMessage(SubathonEventMessageFactory.createSubathonEventMessage(event));
    }

    public void handle(ChannelSubscriptionMessageEvent event) {
        getLogger().debug("Handling ChannelSubscriptionMessageEvent for broadcaster user id '{}'", event.getBroadcasterUserId());
        rabbitMessageService.produceMessage(SubathonEventMessageFactory.createSubathonEventMessage(event));
    }

    public void handle(ChannelSubscriptionGiftEvent event) {
        getLogger().debug("Handling ChannelSubscriptionGiftEvent for broadcaster user id '{}'", event.getBroadcasterUserId());
        rabbitMessageService.produceMessage(SubathonEventMessageFactory.createSubathonEventMessage(event));
    }
}
