package tools.subathon.timer.dataservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.dataservice.service.TimerService;
import tools.subathon.timer.dataservice.service.exception.MissingTimerException;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static tools.subathon.timer.util.GlobalRabbitMQ.TWITCH_EVENT_QUEUE;

@Component
public class MessageReceiver implements HasLogger {
    @Value("${timer.ignoremock}")
    private boolean ignoreMock;

    private final TimerService timerService;

    @Autowired
    public MessageReceiver(TimerService timerService) {
        this.timerService = timerService;
    }

    @RabbitListener(queues = TWITCH_EVENT_QUEUE)
    public void receiveMessage(SubathonEventMessage eventMessage) {
        // TODO: Add null/error handling!
        SubathonEvent event = eventMessage.getSubathonEvent();
        if(event == null) {
            getLogger().warn("Received a message without a subathon event! '{}'", eventMessage);
            return;
        }
        if (event.isMock() && ignoreMock) {
            getLogger().info("Event is mock: {}", event);
            return;
        }
        try {
            timerService.addSubathonEventTime(eventMessage.getChannelId(), event);
        } catch (MissingTimerException e) {
            getLogger().error("Failed to handle twitch event!", e);
        }
    }

}

