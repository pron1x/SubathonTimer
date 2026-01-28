package tools.subathon.timer.ui.component;

import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEventDto;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import elemental.json.Json;
import elemental.json.JsonNumber;

@JsModule("./src/uptime-clock.ts")
@Tag("uptime-clock")
public class UptimeClock extends Component {

    private TimerDto timer;

    public UptimeClock(TimerDto timer) {
        this.timer = timer;
        pushInitialState(timer);
    }

    public void setTimer(TimerDto timer) {
        this.timer = timer;
    }

    public void pushInitialState(TimerDto timer) {
        if(timer == null) return;
        long start = timer.startTime() != null ? timer.startTime().toEpochMilli() : -1;
        long end = timer.endTime() != null ? timer.endTime().toEpochMilli() : -1;
        String state = timer.state().toString();
        getElement().callJsFunction("setState", Json.create(start), Json.create(end), state);
    }

    public void pushState(TimerEventDto event) {
        long start = timer.startTime().toEpochMilli();
        long end = event.currentEndTime().toEpochMilli();
        String state = event.currentTimerState().toString();
        getElement().callJsFunction("setState", Json.create(start), Json.create(end), state);
    }

    @SuppressWarnings("unused")
    @ClientCallable
    public JsonNumber getCurrentServerTimestamp() {
        return Json.create(System.currentTimeMillis());
    }
}
