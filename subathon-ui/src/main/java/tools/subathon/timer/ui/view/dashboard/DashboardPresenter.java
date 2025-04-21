package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.ui.service.RabbitMessageService;
import tools.subathon.timer.ui.service.TimerService;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.List;

@UIScope
@Controller
public class DashboardPresenter implements HasLogger {

    private final RabbitMessageService rabbitMessageService;
    private final TimerService timerService;
    private DashboardView view;

    @Autowired
    public DashboardPresenter(TimerService timerService, RabbitMessageService rabbitMessageService) {
        this.timerService = timerService;
        this.rabbitMessageService = rabbitMessageService;
    }

    protected void init(DashboardView dashboardView) {
        this.view = dashboardView;
        view.initViewInternal();
    }

    protected List<Timer> getAllActiveTimers() {
        return timerService.getAllActiveTimers();
    }

    protected void joinChannel(String channelName) {
        rabbitMessageService.sendRpcToBot(channelName);
    }
}
