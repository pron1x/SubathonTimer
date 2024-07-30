package com.pronixxx.subathon.ui.view.timer;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.ui.component.SubathonTimer;
import com.pronixxx.subathon.ui.service.TimerEventService;
import com.pronixxx.subathon.ui.service.TimerEventService.TimerEventListener;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

@Route(value = "timer")
public class TimerView extends HorizontalLayout implements TimerEventListener, HasLogger {

    private final TimerEventService timerEventService;

    private final SubathonTimer timer;

    private CompletableFuture<Void> stage;

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
                    addTimeAddedTheme();
                })
        );

    }

    private void changeClassName(Component component, String className, boolean remove) {
        getUI().ifPresent(ui -> ui.access(() -> {
            if(remove) {
                component.removeClassName(className);
            } else {
                component.addClassName(className);
            }
        }));
    }

    private void addTimeAddedTheme() {
        if(stage != null && !stage.isDone()) {
            stage.cancel(true);
        } else {
            changeClassName(timer, "neon", false);
        }
        stage = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        stage.thenRun(() -> changeClassName(timer, "neon", true));
    }
}
