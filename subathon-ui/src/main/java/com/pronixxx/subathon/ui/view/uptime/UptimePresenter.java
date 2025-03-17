package com.pronixxx.subathon.ui.view.uptime;

import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.datamodel.enums.TimerState;
import com.pronixxx.subathon.ui.service.TimerEventService;
import com.pronixxx.subathon.ui.service.TimerEventService.TimerEventListener;
import com.pronixxx.subathon.ui.service.TimerService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@UIScope
@Controller
public class UptimePresenter implements TimerEventListener {

    private final TimerService timerService;
    private final TimerEventService timerEventService;

    private UptimeView uptimeView;

    private Timer timer;

    @Autowired
    public UptimePresenter(TimerService timerService, TimerEventService timerEventService) {
        this.timerService = timerService;
        this.timerEventService = timerEventService;
    }

    protected void init(UptimeView uptimeView) {
        this.uptimeView = uptimeView;
    }

    public void onAttach(AttachEvent attachEvent) {
        timerEventService.addEventListener(this);
    }

    public void onDetach(DetachEvent detachEvent) {
        timerEventService.removeEventListener(this);
    }

    // TODO: Handle cases where:
    //          - No previous timer was ever run -> timer is null on init
    //          - Previous timer stopped and new timer is started -> fetch new timer on event?
    public Timer getTimerForChannel(String channelId) {
        if(timer == null || !channelId.equals(timer.getChannelId())) {
            timer = timerService.getTimerForChannel(channelId);
        }
        return timer;
    }

    @Override
    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        if(timerEvent.getTimerId() != timer.getId()) {
            return;
        }

        if(timerEvent.getCurrentTimerState() == TimerState.ENDED ||
                (timerEvent.getCurrentTimerState() == TimerState.TICKING && timerEvent.getOldTimerState() == TimerState.INITIALIZED)) {
            if(timerEvent.getOldTimerState() == TimerState.INITIALIZED) {
                timer = timerService.getTimerForChannel(timer.getChannelId()); // Refetch timer with correct start time!
                uptimeView.getUI().ifPresent(ui -> ui.access(() -> uptimeView.setTimer(timer)));
            }
            uptimeView.getUI().ifPresent(ui -> ui.access(() -> {uptimeView.updateTimerState(timerEvent);}));
        }
    }
}






