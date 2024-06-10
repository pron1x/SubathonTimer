package com.pronixxx.subathon;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {
    public void receiveMessage(String message) {
        LoggerFactory.getLogger(MessageReceiver.class).info("Received Message: {}", message);
    }
}
