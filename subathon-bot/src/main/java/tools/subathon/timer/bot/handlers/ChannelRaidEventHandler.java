package tools.subathon.timer.bot.handlers;

import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.timer.bot.factories.SubathonEventMessageFactory;
import tools.subathon.timer.bot.service.RabbitMessageService;
import tools.subathon.timer.util.interfaces.HasLogger;

@Service
public class ChannelRaidEventHandler implements HasLogger {

    private final RabbitMessageService rabbitMessageService;

    @Autowired
    public ChannelRaidEventHandler(RabbitMessageService rabbitMessageService) {
        this.rabbitMessageService = rabbitMessageService;
    }

    public void handle(ChannelRaidEvent event) {
        getLogger().debug("Handling ChannelFollowEvent for broadcaster user id '{}'", event.getToBroadcasterUserId());
        rabbitMessageService.produceMessage(SubathonEventMessageFactory.createSubathonEventMessage(event));
    }
}
