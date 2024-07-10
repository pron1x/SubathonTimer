package com.pronixxx.subathon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pronixxx.subathon.datamodel.SubathonCommandEvent;
import com.pronixxx.subathon.datamodel.SubathonCommunityGiftEvent;
import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.SubathonSubEvent;
import com.pronixxx.subathon.datamodel.enums.EventType;
import com.pronixxx.subathon.datamodel.enums.SubTier;
import com.pronixxx.subathon.service.TimerService;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MessageReceiver implements HasLogger {
    @Value("${timer.ignoremock}")
    private boolean ignoreMock;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TimerService timerService;

    private final Map<String, Integer> communityGiftFilter = new HashMap<>();

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
            timerService.executeBotCommand((SubathonCommandEvent) event);
        } else if (event.getType() == EventType.GIFT) { // If it's a community gift, we need to filter out incoming sub events!
            SubathonCommunityGiftEvent giftEvent = (SubathonCommunityGiftEvent) event;
            // Add the sender and the amount of gifted subs to the map
            communityGiftFilter.merge(event.getUsername(), giftEvent.getAmount(), Integer::sum);
            // Then log the event
            timerService.addSubathonEventTime(event);
        } else if (event.getType() == EventType.SUBSCRIPTION) {
            SubathonSubEvent subEvent = (SubathonSubEvent) event;
            // If the sub is gifted AND the sender has subs left in the map, remove one from the gifted amounts
            if (subEvent.isGifted() && subEvent.getSender() != null && communityGiftFilter.getOrDefault(subEvent.getSender(), 0) > 0) {
                communityGiftFilter.merge(subEvent.getSender(), -1, Integer::sum);
            } else { // Gift sub is not from any gift bomb, handle as normal sub
                timerService.addSubathonEventTime(event);
            }
        } else { // Everything else gets handled normally
            timerService.addSubathonEventTime(event);
        }
    }
}

