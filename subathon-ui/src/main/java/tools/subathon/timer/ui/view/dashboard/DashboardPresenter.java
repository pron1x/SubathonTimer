package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Controller;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.ui.service.TimerService;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.List;

@UIScope
@Controller
public class DashboardPresenter implements HasLogger {

    private final TimerService timerService;
    private DashboardView view;

    public DashboardPresenter(TimerService timerService) {
        this.timerService = timerService;
    }

    protected void init(DashboardView dashboardView) {
        this.view = dashboardView;
        view.initViewInternal();
    }

    protected List<Timer> getAllActiveTimers() {
        return timerService.getAllActiveTimers();
    }
}
