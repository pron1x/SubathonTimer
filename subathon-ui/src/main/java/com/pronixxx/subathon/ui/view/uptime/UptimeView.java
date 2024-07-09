package com.pronixxx.subathon.ui.view.uptime;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.ui.component.UptimeClock;
import com.pronixxx.subathon.ui.service.TimerEventService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("uptime")
public class UptimeView extends Div implements TimerEventService.TimerEventListener {

    private final TimerEventService timerEventService;

    private final UptimeClock clock;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        timerEventService.addEventListener(this);
        clock.pushState(timerEventService.getLatestTimerEvent());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        timerEventService.removeEventListener(this);
    }

    public UptimeView(@Autowired TimerEventService timerEventService) {
        this.timerEventService = timerEventService;
        TimerEvent initial = timerEventService.getLatestTimerEvent();
        clock = new UptimeClock(initial);
        Div uptimeWrapper = new Div(clock);
        add(uptimeWrapper);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        // We only need to change something with the uptime if we switch from INITIALIZED to TICKING (starting the time first time)
        // or when the timer ends, as we freeze the uptime at that point!
        if(timerEvent.getCurrentTimerState() == TimerState.ENDED ||
                (timerEvent.getCurrentTimerState() == TimerState.TICKING && timerEvent.getOldTimerState() == TimerState.INITIALIZED)) {
            getUI().ifPresent(ui -> ui.access(() -> {
                clock.pushState(timerEvent);
            }));
        }
    }
}
