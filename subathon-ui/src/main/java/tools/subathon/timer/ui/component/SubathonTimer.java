package tools.subathon.timer.ui.component;

import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.util.interfaces.HasLogger;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

@JsModule("./src/subathon-timer.ts")
@Tag("subathon-timer")
public class SubathonTimer extends Component implements HasLogger {

    public SubathonTimer(TimerDto timer) {
        setClassName("subathon-timer");
        pushInitialState(timer);
    }

    public void updateWithNewEvent(TimerEventDto event) {
        if(event != null) {
            pushNewTimerEvent(event);
        }
    }

    private void pushInitialState(TimerDto timer) {
        if(timer == null) return;
        long end = timer.endTime() != null ? timer.endTime().toEpochMilli() : 0;
        long update = timer.updateTime() != null ? timer.updateTime().toEpochMilli() : 0;
        String state = timer.state().toString();
        getElement().callJsFunction("updateToNewTimerEvent", end, update, state);
    }

    private void pushNewTimerEvent(TimerEventDto event) {
        long end = event.currentEndTime().toEpochMilli();
        long update = event.timestamp().toEpochMilli();
        String state = event.currentTimerState().toString();
        getElement().callJsFunction("updateToNewTimerEvent", end, update, state);
    }

    @SuppressWarnings("unused")
    @ClientCallable
    public Long getCurrentServerTimestamp() {
        return System.currentTimeMillis();
    }

}
