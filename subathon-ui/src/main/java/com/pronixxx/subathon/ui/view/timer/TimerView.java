package com.pronixxx.subathon.ui.view.timer;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.ui.component.SubathonTimer;
import com.pronixxx.subathon.ui.service.TimerEventService;
import com.pronixxx.subathon.ui.service.TimerEventService.TimerEventListener;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "timer")
public class TimerView extends HorizontalLayout implements TimerEventListener, HasLogger {

    @Autowired
    private TimerEventService timerEventService;

    private final SubathonTimer timer;


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        timerEventService.addEventListener(this);
        TimerEvent initial = timerEventService.getLatestTimerEvent();
        timer.setStartTime(initial.getStartTime());
        timer.setTimerState(initial.getCurrentTimerState());
        timer.setLastUpdate(initial.getTimestamp());
        timer.setEndTime(initial.getCurrentEndTime());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        timerEventService.removeEventListener(this);
    }

    public TimerView(TimerEventService timerEventService) {
        this.timerEventService = timerEventService;
        TimerEvent initial = timerEventService.getLatestTimerEvent();
        timer = new SubathonTimer(initial.getStartTime(), initial.getCurrentEndTime(),
                initial.getTimestamp(), initial.getCurrentTimerState());
        Div timerWrapper = new Div(timer);
        add(timerWrapper);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        getUI().ifPresent(
                ui -> ui.access(() -> {
                    getLogger().info("Handling TimerEvent: {}", timerEvent);
                    timer.setStartTime(timerEvent.getStartTime());
                    timer.setTimerState(timerEvent.getCurrentTimerState());
                    timer.setEndTime(timerEvent.getCurrentEndTime());
                    timer.setLastUpdate(timerEvent.getTimestamp());
                })
        );

    }
}
