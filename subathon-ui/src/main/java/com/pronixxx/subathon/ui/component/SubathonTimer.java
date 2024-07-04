package com.pronixxx.subathon.ui.component;

import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@JsModule("./src/subathon-timer.ts")
@Tag("subathon-timer")
public class SubathonTimer extends Component {

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private ZonedDateTime lastUpdate;
    private TimerState timerState;


    public SubathonTimer(ZonedDateTime startTime, ZonedDateTime endTime, ZonedDateTime lastUpdate, TimerState timerState) {
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

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
        pushStartTime();
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
        pushEndTime();
    }

    public void setLastUpdate(ZonedDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
        pushLastUpdate();
    }

    public void setTimerState(TimerState timerState) {
        this.timerState = timerState;
        pushTimerState();
    }

    private void pushStartTime() {
        getElement().callJsFunction("setStartTime", startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    private void pushEndTime() {
        getElement().callJsFunction("setEndTime", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    private void pushLastUpdate() {
        getElement().callJsFunction("setLastUpdateTime", lastUpdate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    private void pushTimerState() {
        getElement().callJsFunction("setTimerState", timerState.toString());
    }

}
