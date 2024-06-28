package com.pronixxx.subathon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.enums.EventType;
import com.pronixxx.subathon.service.TimerService;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver implements HasLogger {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TimerService timerService;

    public void receiveMessage(String message) {
        SubathonEvent event;
        try {
            event = objectMapper.readValue(message, SubathonEvent.class);
            getLogger().debug("Received Message: {}", event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (event.isMock()) {
            getLogger().debug("Event is mock: {}", event);
        }

        if (event.getType() == EventType.COMMAND) {
            getLogger().info("Received command!");
        } else {
            timerService.addSubathonEventTime(event);
        }
    }
}
