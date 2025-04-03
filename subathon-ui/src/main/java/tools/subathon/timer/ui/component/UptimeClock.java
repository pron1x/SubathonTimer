package tools.subathon.timer.ui.component;

import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.TimerEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.Json;
import elemental.json.JsonNumber;

@JsModule("./src/uptime-clock.ts")
@Tag("uptime-clock")
public class UptimeClock extends Component {

    private Timer timer;

    public UptimeClock(Timer timer) {
        this.timer = timer;
        pushInitialState(timer);
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public void pushInitialState(Timer timer) {
        if(timer == null) return;
        long start = timer.getStartTime() != null ? timer.getStartTime().toEpochMilli() : -1;
        long end = timer.getEndTime() != null ? timer.getEndTime().toEpochMilli() : -1;
        String state = timer.getState().toString();
        getElement().callJsFunction("setState", Json.create(start), Json.create(end), state);
    }

    public void pushState(TimerEvent event) {
        long start = timer.getStartTime().toEpochMilli();
        long end = event.getCurrentEndTime().toEpochMilli();
        String state = event.getCurrentTimerState().toString();
        getElement().callJsFunction("setState", Json.create(start), Json.create(end), state);
    }

    @SuppressWarnings("unused")
    @ClientCallable
    public JsonNumber getCurrentServerTimestamp() {
        return Json.create(System.currentTimeMillis());
    }
}
