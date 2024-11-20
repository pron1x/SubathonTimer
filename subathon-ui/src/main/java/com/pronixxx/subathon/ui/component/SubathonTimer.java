package com.pronixxx.subathon.ui.component;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.Json;
import elemental.json.JsonNumber;

@JsModule("./src/subathon-timer.ts")
@Tag("subathon-timer")
public class SubathonTimer extends Component implements HasLogger {

    public SubathonTimer(TimerEvent event) {
        updateWithNewEvent(event);
    }

    public void updateWithNewEvent(TimerEvent event) {
        if(event != null) {
            pushNewTimerEvent(event);
        }
    }

    private void pushNewTimerEvent(TimerEvent event) {
        long start = event.getStartTime().toEpochMilli();
        long end = event.getCurrentEndTime().toEpochMilli();
        long update = event.getTimestamp().toEpochMilli();
        String state = event.getCurrentTimerState().toString();
        getElement().callJsFunction("updateToNewTimerEvent", Json.create(start), Json.create(end), Json.create(update), state);
    }

    @SuppressWarnings("unused")
    @ClientCallable
    public JsonNumber getCurrentServerTimestamp() {
        return Json.create(System.currentTimeMillis());
    }

}
