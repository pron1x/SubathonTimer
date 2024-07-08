package com.pronixxx.subathon.ui.component;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@JsModule("./src/subathon-timer.ts")
@Tag("subathon-timer")
public class SubathonTimer extends Component implements HasLogger {

    private final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


    public SubathonTimer(TimerEvent event) {
        updateWithNewEvent(event);
    }

    public void updateWithNewEvent(TimerEvent event) {
        if(event != null) {
            pushNewTimerEvent(event);
        }
    }

    private void pushNewTimerEvent(TimerEvent event) {
        String start = event.getStartTime().atOffset(ZoneOffset.UTC).format(UTC_FORMATTER);
        String end = event.getCurrentEndTime().atOffset(ZoneOffset.UTC).format(UTC_FORMATTER);
        String update = event.getTimestamp().atOffset(ZoneOffset.UTC).format(UTC_FORMATTER);
        String state = event.getCurrentTimerState().toString();
        getElement().callJsFunction("updateToNewTimerEvent", start, end, update, state);
    }

}
