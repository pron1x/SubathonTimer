package com.pronixxx.subathon.ui.view.timer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "timer")
public class TimerView extends Div {

    public TimerView() {
        Div timerWrapper = new Div();
        timerWrapper.setText("This is the timer");
        add(timerWrapper);
    }
}
