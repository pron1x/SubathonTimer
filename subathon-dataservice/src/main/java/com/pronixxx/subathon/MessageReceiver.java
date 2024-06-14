package com.pronixxx.subathon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.SubathonFollowerEvent;
import com.pronixxx.subathon.service.TimerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TimerService timerService;

    public void receiveMessage(String message) {
        SubathonEvent event;
        try {
            event = objectMapper.readValue(message, SubathonEvent.class);
            LoggerFactory.getLogger(MessageReceiver.class).info("Received Message: {}", event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if(event.isMock()) {
            LoggerFactory.getLogger(MessageReceiver.class).info("Event is mock: {}", event);
        }
        switch (event.getType()) {
            case FOLLOW:
                timerService.addFollowToTimer((SubathonFollowerEvent) event);
                break;
            default:
        }
    }
}
