package tools.subathon.timer.bot.handlers;

import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import org.springframework.stereotype.Component;
import tools.subathon.timer.bot.factories.SubathonEventMessageFactory;
import tools.subathon.timer.bot.service.RabbitMessageService;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.util.interfaces.HasLogger;

@Component
public class ChannelChatNotificationHandler implements HasLogger {

    private final RabbitMessageService rabbitMessageService;

    public ChannelChatNotificationHandler(RabbitMessageService rabbitMessageService) {
        this.rabbitMessageService = rabbitMessageService;
    }

    public void handle(ChannelChatNotificationEvent event) {
        try {
            SubathonEventMessage eventMessage = SubathonEventMessageFactory.createSubathonSubscribeEventFromNotification(event);
            rabbitMessageService.produceMessage(eventMessage);
        } catch (IllegalArgumentException e) {
            getLogger().warn("Error turning ChannelChatNotificationEvent into SubathonEventMessage: {}", e.getMessage());
        }
    }
}
