package tools.subathon.timer.ui.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.TimerEvent;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;

import static tools.subathon.timer.util.GlobalRabbitMQ.EVENT_QUEUE_NAME;

@Service
public class MessageReceiver implements HasLogger {

    private final ObjectMapper objectMapper;

    private final TimerEventService timerEventService;

    @Autowired
    public MessageReceiver(ObjectMapper objectMapper, TimerEventService timerEventService) {
        this.objectMapper = objectMapper;
        this.timerEventService = timerEventService;
    }

    @RabbitListener(queues = EVENT_QUEUE_NAME)
    public void receiveMessage(String message) {
        TimerEvent timerEvent = null;
        try {
            timerEvent = objectMapper.readValue(message, TimerEvent.class);
        } catch (JsonProcessingException e) {
            getLogger().warn("Could not convert incoming TimerEvent message to object! {}", message, e);
        }
        timerEventService.handleIncomingTimerEvent(timerEvent);
    }
}
