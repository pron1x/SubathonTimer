package tools.subathon.timer.ui.component;

import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.signals.Signal;
import tools.subathon.timer.util.interfaces.HasLogger;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

@JsModule("./subathon-timer-adapter.tsx")
@Tag("subathon-timer")
public class SubathonTimer extends ReactAdapterComponent implements HasLogger {

    public SubathonTimer() {
        setClassName("subathon-timer");
    }

    public void bindEndtime(Signal<Long> signal) {
        getElement().bindProperty("endTime", signal.map(l -> l != null ? l.doubleValue() : null), null);
    }

    public void bindLastUpdateTime(Signal<Long> signal) {
        getElement().bindProperty("lastUpdateTime", signal.map(l -> l != null ? l.doubleValue() : null), null);
    }

    public void bindTimerState(Signal<String> signal) {
        getElement().bindProperty("timerState", signal, null);
    }

    public void bindServerTime(Signal<Long> signal) {
        getElement().bindProperty("serverTime", signal.map(l -> l != null ? l.doubleValue() : null), null);
    }

}
