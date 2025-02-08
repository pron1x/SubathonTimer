package com.pronixxx.subathon.ui.view.timer;

import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.datamodel.enums.TimerEventType;
import com.pronixxx.subathon.ui.component.SubathonTimer;
import com.pronixxx.subathon.ui.service.TimerEventService;
import com.pronixxx.subathon.ui.service.TimerEventService.TimerEventListener;
import com.pronixxx.subathon.ui.service.TimerService;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

@Route(value = "timer/")
public class TimerView extends HorizontalLayout implements TimerEventListener, HasLogger, HasUrlParameter<String> {

    private final TimerEventService timerEventService;

    private final TimerService timerService;

    private Timer timer;
    private SubathonTimer timerComponent;

    private CompletableFuture<Void> stage;

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
    public TimerView(TimerEventService timerEventService, TimerService timerService) {
        this.timerEventService = timerEventService;
        this.timerService = timerService;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        timer = timerService.getTimerForChannel(s);
        timerComponent = new SubathonTimer(timer);
        Div timerWrapper = new Div(timerComponent);
        add(timerWrapper);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        // Filter for relevant timerEvents
        if(timerEvent.getTimerId() != timer.getId()) {
            return;
        }
        getUI().ifPresent(
                ui -> ui.access(() -> {
                    getLogger().info("Handling TimerEvent: {}", timerEvent);
                    timerComponent.updateWithNewEvent(timerEvent);
                    if(timerEvent.getType() == TimerEventType.TIME_ADDITION) {
                        addTimeAddedTheme();
                    }
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

    // FIXME: With multiple timers the neon doesn't seem to disappear
    private void addTimeAddedTheme() {
        if(stage != null && !stage.isDone()) {
            stage.cancel(true);
        } else {
            changeClassName(timerComponent, "neon", false);
        }
        stage = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        stage.thenRun(() -> changeClassName(timerComponent, "neon", true));
    }
}
