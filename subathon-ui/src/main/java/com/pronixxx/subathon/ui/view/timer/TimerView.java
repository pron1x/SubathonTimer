package com.pronixxx.subathon.ui.view.timer;

import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.ui.component.SubathonTimer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

@Route(value = "timer")
public class TimerView extends HorizontalLayout {

    ZonedDateTime startTime;
    ZonedDateTime endTime;
    ZonedDateTime lastUpdate;
    TimerState state;
    Clock clock = Clock.systemUTC();

    public TimerView() {
        lastUpdate = ZonedDateTime.now(clock);
        startTime = ZonedDateTime.now(clock);
        endTime = ZonedDateTime.now(clock).plusMinutes(10);
        state = TimerState.INITIALIZED;
        SubathonTimer timer = new SubathonTimer(startTime, endTime, lastUpdate, state);
        Div timerWrapper = new Div(timer);
        add(timerWrapper);

        VerticalLayout controls = new VerticalLayout();
        Button start = new Button("Start");
        start.addClickListener(event -> {
            if(state == TimerState.PAUSED) {
                endTime = endTime.plusSeconds(Duration.between(lastUpdate, ZonedDateTime.now(clock)).toSeconds());
            }
            state = TimerState.TICKING;
            lastUpdate = ZonedDateTime.now(clock);

            timer.setTimerState(state);
            timer.setEndTime(endTime);
            timer.setLastUpdate(lastUpdate);
        });

        Button pause = new Button("Pause");
        pause.addClickListener(event -> {
            if(state == TimerState.ENDED || state == TimerState.INITIALIZED) {
                return;
            }
            state = TimerState.PAUSED;
            lastUpdate = ZonedDateTime.now(clock);
            timer.setTimerState(state);
            timer.setLastUpdate(lastUpdate);
        });

        Button addTime = new Button("Add 1m");
        addTime.addClickListener(event -> {
            if(state == TimerState.ENDED || state == TimerState.INITIALIZED) {
                return;
            }
            if(state == TimerState.PAUSED) {
                endTime = endTime.plusSeconds(Duration.between(lastUpdate, ZonedDateTime.now(clock)).toSeconds());
            }
            lastUpdate = ZonedDateTime.now(clock);
            endTime = endTime.plusMinutes(1);
            timer.setEndTime(endTime);
            timer.setLastUpdate(lastUpdate);
        });

        controls.add(start, pause, addTime);
        add(controls);
    }
}
