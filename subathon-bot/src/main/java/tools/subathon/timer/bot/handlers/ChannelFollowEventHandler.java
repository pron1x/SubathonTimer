package tools.subathon.timer.bot.handlers;

import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.timer.bot.service.RabbitMessageService;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.SubathonFollowerEvent;
import tools.subathon.timer.util.interfaces.HasLogger;

@Service
public class ChannelFollowEventHandler implements HasLogger {

    private final static String SOURCE = "twitch-event-sub";

    private final RabbitMessageService rabbitMessageService;

    @Autowired
    public ChannelFollowEventHandler(RabbitMessageService rabbitMessageService) {
        this.rabbitMessageService = rabbitMessageService;
    }

    public void handle(ChannelFollowEvent event) {
        getLogger().debug("Handling ChannelFollowEvent for broadcaster user id '{}'", event.getBroadcasterUserId());
        rabbitMessageService.produceMessage(createSubathonEventMessage(event));
    }

    private SubathonEventMessage createSubathonEventMessage(ChannelFollowEvent event) {
        SubathonEventMessage eventMessage = new SubathonEventMessage();
        eventMessage.setChannelId(event.getBroadcasterUserId());
        eventMessage.setSubathonEvent(createSubathonFollowerEvent(event));
        return eventMessage;
    }

    private SubathonFollowerEvent createSubathonFollowerEvent(ChannelFollowEvent event) {
        SubathonFollowerEvent followerEvent = new SubathonFollowerEvent();
        followerEvent.setSource(SOURCE);
        followerEvent.setTimestamp(event.getFollowedAt());
        followerEvent.setUsername(event.getUserName());

        return followerEvent;
    }
}
