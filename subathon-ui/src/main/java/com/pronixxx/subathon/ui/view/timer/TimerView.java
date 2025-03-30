package com.pronixxx.subathon.ui.view.timer;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.ui.component.SubathonTimer;
import com.pronixxx.subathon.util.interfaces.HasLogger;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "timer/")
public class TimerView extends HorizontalLayout implements HasLogger, HasUrlParameter<String> {

    private final TimerPresenter timerPresenter;

    private SubathonTimer timerComponent;

    @Autowired
    public TimerView(TimerPresenter timerPresenter) {
        this.timerPresenter = timerPresenter;
    }

    @PostConstruct
    private void init() {
        timerPresenter.init(this);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        timerPresenter.onAttach(attachEvent);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        timerPresenter.onDetach(detachEvent);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        timerComponent = new SubathonTimer(timerPresenter.getTimerForChannel(s));
        Div timerWrapper = new Div(timerComponent);
        add(timerWrapper);
    }

    public void updateTimer(TimerEvent timerEvent) {
        timerComponent.updateWithNewEvent(timerEvent);

    }

    public void setTimerClassName(String className) {
        timerComponent.addClassName(className);
    }

    public void removeTimerClassName(String className) {
        timerComponent.removeClassName(className);
    }
}
