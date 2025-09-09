package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.ui.service.BotRpcService;
import tools.subathon.timer.ui.service.DataserviceRpcService;
import tools.subathon.timer.ui.service.TimerService;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.List;

@UIScope
@Controller
public class DashboardPresenter implements HasLogger {

    private final BotRpcService botRpcService;
    private final DataserviceRpcService dataserviceRpcService;
    private final TimerService timerService;
    private DashboardView view;

    @Autowired
    public DashboardPresenter(TimerService timerService, BotRpcService botRpcService, DataserviceRpcService dataserviceRpcService) {
        this.timerService = timerService;
        this.botRpcService = botRpcService;
        this.dataserviceRpcService = dataserviceRpcService;
    }

    protected void init(DashboardView dashboardView) {
        this.view = dashboardView;
        view.initViewInternal();
    }

    protected List<Timer> getAllActiveTimers() {
        return timerService.getAllActiveTimers();
    }

    protected Timer getTimerFor(String channelId) {
        return timerService.getTimerForChannel(channelId);
    }

    protected boolean initializeTimer(String channelId, String channelName) {
        return dataserviceRpcService.initializeTimerForChannel(channelId, channelName);
    }

    protected UserConfigurationModel getUserConfig(String userId) {
        return dataserviceRpcService.getUserConfiguration(userId);
    }

    protected void saveUserConfig(String userId, UserConfigurationModel userConfigurationModel) {
        userConfigurationModel.setChannelId(userId);
        dataserviceRpcService.saveUserConfiguration(userId, userConfigurationModel);
    }
}
