package tools.subathon.timer.dataservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.enums.EventType;
import tools.subathon.timer.dataservice.service.TimerService;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static tools.subathon.timer.util.GlobalRabbitMQ.DATASERVICE_RPC_QUEUE_NAME;
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
        if (event.isMock() && ignoreMock) {
            getLogger().info("Event is mock: {}", event);
            return;
        }
        timerService.addSubathonEventTime(eventMessage.getChannelId(), event);
    }

    @RabbitListener(queues = DATASERVICE_RPC_QUEUE_NAME)
    public Boolean handleBotCommand(SubathonEventMessage eventMessage) {
        SubathonEvent command = eventMessage.getSubathonEvent();
        if (command.getType() != EventType.COMMAND) {
            return false;
        }
        timerService.executeBotCommand(eventMessage.getChannelId(), (SubathonCommandEvent) command);
        return true;
    }
}

