package com.pronixxx.subathon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.SubathonFollowerEvent;
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
            getLogger().info("Received Message: {}", event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if(event.isMock()) {
            getLogger().info("Event is mock: {}", event);
        }
        switch (event.getType()) {
            case FOLLOW:
                timerService.addFollowToTimer((SubathonFollowerEvent) event);
                break;
            default:
        }
    }
}
