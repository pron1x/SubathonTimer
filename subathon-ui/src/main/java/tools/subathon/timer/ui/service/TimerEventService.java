package tools.subathon.timer.ui.service;

import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.util.interfaces.HasLogger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TimerEventService implements HasLogger {

    private final List<TimerEventListener> listeners = new ArrayList<>();

    public void handleIncomingTimerEvent(TimerEventDto timerEventDto) {
        for (TimerEventListener listener : listeners) {
            try {
                listener.handleIncomingTimerEvent(timerEventDto);
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
        void handleIncomingTimerEvent(TimerEventDto timerEventDto);
    }

}
