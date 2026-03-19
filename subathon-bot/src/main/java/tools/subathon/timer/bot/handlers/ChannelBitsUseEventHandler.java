package tools.subathon.timer.bot.handlers;

import com.github.twitch4j.eventsub.events.ChannelBitsUseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.timer.bot.factories.SubathonEventMessageFactory;
import tools.subathon.timer.bot.service.RabbitMessageService;
import tools.subathon.timer.util.interfaces.HasLogger;

@Service
public class ChannelBitsUseEventHandler implements HasLogger {

    private final RabbitMessageService rabbitMessageService;

    @Autowired
    public ChannelBitsUseEventHandler(RabbitMessageService rabbitMessageService) {
        this.rabbitMessageService = rabbitMessageService;
    }

    public void handle(ChannelBitsUseEvent event) {
        getLogger().debug("Handling ChannelBitsUseEvent for broadcaster user id '{}'", event.getBroadcasterUserId());
        rabbitMessageService.produceMessage(SubathonEventMessageFactory.createSubathonEventMessage(event));
    }
}
