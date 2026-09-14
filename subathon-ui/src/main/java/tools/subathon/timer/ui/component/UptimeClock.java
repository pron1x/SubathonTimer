package tools.subathon.timer.ui.component;

import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import tools.subathon.timer.util.interfaces.HasLogger;

@JsModule("./src/uptime-clock-adapter.tsx")
@Tag("uptime-clock")
public class UptimeClock extends ReactAdapterComponent implements HasLogger {

    public UptimeClock() {
        setClassName("uptime-clock");
    }

    public void bindStartTime(Signal<Long> signal) {
        getElement().bindProperty("startTime", signal.map(l -> l != null ? l.doubleValue() : null), null);
    }

    public void bindEndTime(Signal<Long> signal) {
        getElement().bindProperty("endTime", signal.map(l -> l != null ? l.doubleValue() : null), null);
    }

    public void bindTimerState(Signal<String> signal) {
        getElement().bindProperty("timerState", signal, null);
    }

    public void bindServerTime(Signal<Long> signal) {
        getElement().bindProperty("serverTime", signal.map(l -> l != null ? l.doubleValue() : null), null);
    }
}
