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

    private final TimerEventService timerEventService;

    private final SubathonTimer timer;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        timerEventService.addEventListener(this);
        timer.updateWithNewEvent(timerEventService.getLatestTimerEvent());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        timerEventService.removeEventListener(this);
    }

    public TimerView(@Autowired TimerEventService timerEventService) {
        this.timerEventService = timerEventService;
        TimerEvent initial = timerEventService.getLatestTimerEvent();
        timer = new SubathonTimer(initial);
        Div timerWrapper = new Div(timer);
        add(timerWrapper);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        getUI().ifPresent(
                ui -> ui.access(() -> {
                    getLogger().info("Handling TimerEvent: {}", timerEvent);
                    timer.updateWithNewEvent(timerEvent);
                })
        );

    }
}
