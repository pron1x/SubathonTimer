package com.pronixxx.subathon.ui.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver implements HasLogger {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TimerEventService timerEventService;

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
