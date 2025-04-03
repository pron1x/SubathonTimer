package tools.subathon.timer.ui.component;

import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.TimerEvent;
import tools.subathon.timer.util.interfaces.HasLogger;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.Json;
import elemental.json.JsonNumber;

@JsModule("./src/subathon-timer.ts")
@Tag("subathon-timer")
public class SubathonTimer extends Component implements HasLogger {

    public SubathonTimer(Timer timer) {
        pushInitialState(timer);
    }

    public void updateWithNewEvent(TimerEvent event) {
        if(event != null) {
            pushNewTimerEvent(event);
        }
    }

    private void pushInitialState(Timer timer) {
        long end = timer.getEndTime() != null ? timer.getEndTime().toEpochMilli() : 0;
        long update = timer.getUpdateTime() != null ? timer.getUpdateTime().toEpochMilli() : 0;
        String state = timer.getState().toString();
        getElement().callJsFunction("updateToNewTimerEvent", Json.create(end), Json.create(update), state);
    }

    private void pushNewTimerEvent(TimerEvent event) {
        long end = event.getCurrentEndTime().toEpochMilli();
        long update = event.getTimestamp().toEpochMilli();
        String state = event.getCurrentTimerState().toString();
        getElement().callJsFunction("updateToNewTimerEvent", Json.create(end), Json.create(update), state);
    }

    @SuppressWarnings("unused")
    @ClientCallable
    public JsonNumber getCurrentServerTimestamp() {
        return Json.create(System.currentTimeMillis());
    }

}
