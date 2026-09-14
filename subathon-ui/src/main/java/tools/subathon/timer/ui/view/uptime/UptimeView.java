package tools.subathon.timer.ui.view.uptime;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import tools.subathon.timer.ui.component.UptimeClock;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import tools.subathon.timer.ui.config.ServerTimeSignal;

@Route("uptime")
@AnonymousAllowed
public class UptimeView extends Div implements HasUrlParameter<String> {

    private final UptimePresenter uptimePresenter;
    private final ServerTimeSignal serverTimeSignal;

    private UptimeClock clock;

    @Autowired
    public UptimeView(UptimePresenter uptimePresenter, ServerTimeSignal serverTimeSignal) {
        this.uptimePresenter = uptimePresenter;
        this.serverTimeSignal = serverTimeSignal;
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
        clock = new UptimeClock();
        uptimePresenter.initForChannel(s);
        clock.bindStartTime(uptimePresenter.getStartTimeSignal());
        clock.bindEndTime(uptimePresenter.getEndTimeSignal());
        clock.bindTimerState(uptimePresenter.getTimerStateSignal());
        clock.bindServerTime(serverTimeSignal.getServerTimeSignal());
        Div uptimeWrapper = new Div(clock);
        add(uptimeWrapper);
    }

}
