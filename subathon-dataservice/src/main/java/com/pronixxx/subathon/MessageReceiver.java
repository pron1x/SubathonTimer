package com.pronixxx.subathon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.datamodel.enums.EventType;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

@Component
public class MessageReceiver {
    @Autowired
    ObjectMapper objectMapper;

    Timer timer = new Timer();

    HashMap<EventType, Integer> secondsPerEvent = new HashMap<>();

    @PostConstruct
    public void init() {
        secondsPerEvent.put(EventType.FOLLOW, 10);
        secondsPerEvent.put(EventType.SUBSCRIPTION, 300);
        secondsPerEvent.put(EventType.TIP, 1);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        timer.setStartTime(now);
        timer.setEndTime(now.plusMinutes(60));
        timer.setState(TimerState.INITIALIZED);
    }

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
        if(timer.getState() == TimerState.INITIALIZED) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
            Duration duration = Duration.between(timer.getStartTime(), now);
            timer.setStartTime(now);

            timer.setEndTime(timer.getEndTime().plusSeconds(duration.toSeconds()));
            timer.setState(TimerState.TICKING);
        }
        LoggerFactory.getLogger(MessageReceiver.class).info("[PRE]  Timer End: {}", timer.getEndTime());
        switch (event.getType()) {
            // For now the timer is always ticking, we don't need to account for paused last update duration!
            case FOLLOW:
                timer.setEndTime(timer.getEndTime().plusSeconds(secondsPerEvent.get(event.getType())));
                break;
            default:
        }
        LoggerFactory.getLogger(MessageReceiver.class).info("[POST] Timer End: {}", timer.getEndTime());
    }
}
