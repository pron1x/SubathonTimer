package com.pronixxx.subathon.ui.component;

import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@JsModule("./src/subathon-timer.ts")
@Tag("subathon-timer")
public class SubathonTimer extends Component implements HasLogger {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdate;
    private TimerState timerState;

    private final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


    public SubathonTimer(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime lastUpdate, TimerState timerState) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastUpdate = lastUpdate;
        this.timerState = timerState;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        pushStartTime();
        pushLastUpdate();
        pushEndTime();
        pushTimerState();

    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        pushStartTime();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        pushEndTime();
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
        pushLastUpdate();
    }

    public void setTimerState(TimerState timerState) {
        this.timerState = timerState;
        pushTimerState();
    }

    private void pushStartTime() {
        getElement().callJsFunction("setStartTime", startTime.atOffset(ZoneOffset.UTC).format(UTC_FORMATTER));
    }

    private void pushEndTime() {
        getElement().callJsFunction("setEndTime", endTime.atOffset(ZoneOffset.UTC).format(UTC_FORMATTER));
    }

    private void pushLastUpdate() {
        getElement().callJsFunction("setLastUpdateTime", lastUpdate.atOffset(ZoneOffset.UTC).format(UTC_FORMATTER));
    }

    private void pushTimerState() {
        getElement().callJsFunction("setTimerState", timerState.toString());
    }

}
