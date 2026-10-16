package tools.subathon.timer.bot.handlers;

import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import org.springframework.stereotype.Component;
import tools.subathon.timer.bot.factories.SubathonEventMessageFactory;
import tools.subathon.timer.bot.service.RabbitMessageService;
import tools.subathon.timer.util.interfaces.HasLogger;

@Component
public class ChannelFollowEventHandler implements HasLogger {

    private final RabbitMessageService rabbitMessageService;

    public ChannelFollowEventHandler(RabbitMessageService rabbitMessageService) {
        this.rabbitMessageService = rabbitMessageService;
    }

    public void handle(ChannelFollowEvent event) {
        getLogger().debug("Handling ChannelFollowEvent for broadcaster user id '{}'", event.getBroadcasterUserId());
        rabbitMessageService.produceMessage(SubathonEventMessageFactory.createSubathonEventMessage(event));
    }

}
