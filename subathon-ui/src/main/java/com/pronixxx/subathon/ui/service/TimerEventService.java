package com.pronixxx.subathon.ui.service;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class TimerEventService implements HasLogger {

    private final List<TimerEventListener> listeners = new ArrayList<>();

    @Value("${ui.endpoints.dataservice}")
    private String REQUEST_URI;
    private final RestClient restClient = RestClient.create();

    public TimerEvent getLatestTimerEvent() {
        return restClient.get().uri(REQUEST_URI + "/timer").retrieve().toEntity(TimerEvent.class).getBody();
    }

    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        for (TimerEventListener listener : listeners) {
            try {
                listener.handleIncomingTimerEvent(timerEvent);
            } catch (Exception e) {
                getLogger().warn("Error when handling a timer event listener", e);
            }
        }
    }

    public void addEventListener(TimerEventListener listener) {
        if(listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeEventListener(TimerEventListener listener) {
        if(listener != null) {
            listeners.remove(listener);
        }
    }

    public interface TimerEventListener {
        void handleIncomingTimerEvent(TimerEvent timerEvent);
    }

}
