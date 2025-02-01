package com.pronixxx.subathon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.datamodel.SubathonCommandEvent;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.enums.EventType;
import com.pronixxx.subathon.service.TimerService;
import com.pronixxx.subathon.util.interfaces.HasLogger;
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

    public void receiveMessage(String message) {
        SubathonEvent event;
        try {
            event = objectMapper.readValue(message, SubathonEvent.class);
            getLogger().debug("Received Message: {}", event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (event.isMock() && ignoreMock) {
            getLogger().debug("Event is mock: {}", event);
            return;
        }

        if (event.getType() == EventType.COMMAND) { // We handle bot commands differently
            timerService.executeBotCommand("TEST", (SubathonCommandEvent) event);
        } else { // Everything else gets handled normally
            timerService.addSubathonEventTime("TEST", event);
        }
    }
}

