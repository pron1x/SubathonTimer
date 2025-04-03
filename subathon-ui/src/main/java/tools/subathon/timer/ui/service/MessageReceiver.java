package tools.subathon.timer.ui.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.subathon.timer.datamodel.TimerEvent;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver implements HasLogger {

    private final ObjectMapper objectMapper;

    private final TimerEventService timerEventService;

    @Autowired
    public MessageReceiver(ObjectMapper objectMapper, TimerEventService timerEventService) {
        this.objectMapper = objectMapper;
        this.timerEventService = timerEventService;
    }

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
