package tools.subathon.timer.dataservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.enums.EventType;
import tools.subathon.timer.dataservice.service.TimerService;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver implements HasLogger {
    @Value("${timer.ignoremock}")
    private boolean ignoreMock;

    private final ObjectMapper objectMapper;

    private final TimerService timerService;

    @Autowired
    public MessageReceiver(ObjectMapper objectMapper, TimerService timerService) {
        this.objectMapper = objectMapper;
        this.timerService = timerService;
    }

    // TODO: Add different listener methods for the bot and event queue!
    public void receiveMessage(String message) {
        SubathonEventMessage eventMessage;
        try {
            eventMessage = objectMapper.readValue(message, SubathonEventMessage.class);
            getLogger().debug("Received Message: {}", eventMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // TODO: Add null/error handling!
        SubathonEvent event = eventMessage.getSubathonEvent();
        if (event.isMock() && ignoreMock) {
            getLogger().debug("Event is mock: {}", event);
            return;
        }

        if (event.getType() == EventType.COMMAND) { // We handle bot commands differently
            timerService.executeBotCommand(eventMessage.getChannelId(), (SubathonCommandEvent) event);
        } else { // Everything else gets handled normally
            timerService.addSubathonEventTime(eventMessage.getChannelId(), event);
        }
    }
}

