package com.pronixxx.subathon.ui.component;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@JsModule("./src/uptime-clock.ts")
@Tag("uptime-clock")
public class UptimeClock extends Component {

    private final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


    public UptimeClock(TimerEvent event) {
        pushState(event);
    }

    public void pushState(TimerEvent event) {
        String start = event.getStartTime().atOffset(ZoneOffset.UTC).format(UTC_FORMATTER);
        String end = event.getCurrentEndTime().atOffset(ZoneOffset.UTC).format(UTC_FORMATTER);
        String state = event.getCurrentTimerState().toString();
        getElement().callJsFunction("setState", start, end, state);
    }
}
