package tools.subathon.timer.ui.view.timer;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import tools.subathon.timer.ui.component.SubathonTimer;
import tools.subathon.timer.ui.config.ServerTimeSignal;
import tools.subathon.timer.util.interfaces.HasLogger;
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
@AnonymousAllowed
public class TimerView extends HorizontalLayout implements HasLogger, HasUrlParameter<String> {

    private final TimerPresenter timerPresenter;
    private final ServerTimeSignal serverTimeSignal;

    private SubathonTimer timerComponent;

    @Autowired
    public TimerView(TimerPresenter timerPresenter, ServerTimeSignal serverTimeSignal) {
        this.timerPresenter = timerPresenter;
        this.serverTimeSignal = serverTimeSignal;
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
        timerComponent = new SubathonTimer();
        timerPresenter.initForChannel(s);
        timerComponent.bindEndtime(timerPresenter.getEndTimeSignal());
        timerComponent.bindLastUpdateTime(timerPresenter.getLastUpdateTimeSignal());
        timerComponent.bindTimerState(timerPresenter.getTimerStateSignal());
        timerComponent.bindServerTime(serverTimeSignal.getServerTimeSignal());
        Div timerWrapper = new Div(timerComponent);
        add(timerWrapper);
    }

}
