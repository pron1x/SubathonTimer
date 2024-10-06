package com.pronixxx.subathon.ui.component;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.Json;

import java.time.ZoneOffset;

@JsModule("./src/uptime-clock.ts")
@Tag("uptime-clock")
public class UptimeClock extends Component {

    public UptimeClock(TimerEvent event) {
        pushState(event);
    }

    public void pushState(TimerEvent event) {
        long start = event.getStartTime().toEpochSecond(ZoneOffset.UTC) * 1000L;
        long end = event.getCurrentEndTime().toEpochSecond(ZoneOffset.UTC) * 1000L;
        String state = event.getCurrentTimerState().toString();
        getElement().callJsFunction("setState", Json.create(start), Json.create(end), state);
    }
}
