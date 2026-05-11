package tools.subathon.timer.ui.view.uptime;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.ui.component.UptimeClock;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

@Route("uptime")
@AnonymousAllowed
public class UptimeView extends Div implements HasUrlParameter<String> {

    private final UptimePresenter uptimePresenter;

    private UptimeClock clock;

    @Autowired
    public UptimeView(UptimePresenter uptimePresenter) {
        this.uptimePresenter = uptimePresenter;
    }

    @PostConstruct
    private void init() {
        uptimePresenter.init(this);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        uptimePresenter.onAttach(attachEvent);
        super.onAttach(attachEvent);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        uptimePresenter.onDetach(detachEvent);
        super.onDetach(detachEvent);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        clock = new UptimeClock(uptimePresenter.getTimerForChannel(s));
        Div uptimeWrapper = new Div(clock);
        add(uptimeWrapper);
    }

    public void updateTimerState(TimerEventDto timerEventDto) {
        clock.pushState(timerEventDto);
    }

    public void setTimer(TimerDto timer) {
        clock.setTimer(timer);
    }
}
