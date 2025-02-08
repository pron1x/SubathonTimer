package com.pronixxx.subathon.ui.view.uptime;

import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.ui.component.UptimeClock;
import com.pronixxx.subathon.ui.service.TimerEventService;
import com.pronixxx.subathon.ui.service.TimerService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("uptime")
public class UptimeView extends Div implements TimerEventService.TimerEventListener, HasUrlParameter<String> {

    private final TimerService timerService;

    private final TimerEventService timerEventService;

    private Timer timer;

    private UptimeClock clock;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        timerEventService.addEventListener(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        timerEventService.removeEventListener(this);
    }

    @Autowired
    public UptimeView(TimerService timerService, TimerEventService timerEventService) {
        this.timerService = timerService;
        this.timerEventService = timerEventService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        timer = timerService.getTimerForChannel(s);
        clock = new UptimeClock(timer);
        Div uptimeWrapper = new Div(clock);
        add(uptimeWrapper);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        // Filter for relevant timerEvents
        if(timerEvent.getTimerId() != timer.getId()) {
            return;
        }
        // We only need to change something with the uptime if we switch from INITIALIZED to TICKING (starting the time first time)
        // or when the timer ends, as we freeze the uptime at that point!
        if(timerEvent.getCurrentTimerState() == TimerState.ENDED ||
                (timerEvent.getCurrentTimerState() == TimerState.TICKING && timerEvent.getOldTimerState() == TimerState.INITIALIZED)) {
            if(timerEvent.getOldTimerState() == TimerState.INITIALIZED) { // Set the correct start time if old state was initialized
                timer = timerService.getTimerForChannel(timer.getChannelId());
                getUI().ifPresent(ui -> ui.access(() -> clock.setTimer(timer)));
            }
            getUI().ifPresent(ui -> ui.access(() -> clock.pushState(timerEvent)));
        }
    }
}
