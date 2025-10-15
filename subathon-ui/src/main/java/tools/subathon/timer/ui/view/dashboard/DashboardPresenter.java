package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.TimerEvent;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.datamodel.enums.TimerState;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.ui.service.DataserviceRpcService;
import tools.subathon.timer.ui.service.TimerEventService;
import tools.subathon.timer.ui.service.TimerEventService.TimerEventListener;
import tools.subathon.timer.ui.service.TimerService;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.List;

@UIScope
@Component
public class DashboardPresenter implements TimerEventListener, HasLogger {

    private final String channelId;
    private final String channelName;
    private final DataserviceRpcService dataserviceRpcService;
    private final TimerEventService timerEventService;
    private final TimerService timerService;
    private DashboardView view;

    @Autowired
    public DashboardPresenter(TimerService timerService, DataserviceRpcService dataserviceRpcService, TimerEventService timerEventService) {
        this.timerService = timerService;
        this.dataserviceRpcService = dataserviceRpcService;
        this.timerEventService = timerEventService;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof DefaultOAuth2User oauth2User) {
            this.channelId = oauth2User.getAttribute("sub");
            this.channelName = oauth2User.getName();
        } else {
            throw new AccessDeniedException("User is not authenticated!");
        }
    }

    protected void init(DashboardView dashboardView) {
        this.view = dashboardView;
        view.initViewInternal();
    }

    protected List<Timer> getAllActiveTimers() {
        return timerService.getAllActiveTimers();
    }

    protected Timer getTimer() {
        return timerService.getTimerForChannel(channelId);
    }

    protected void initializeTimer() {
        dataserviceRpcService.initializeTimerForChannel(channelId, channelName);
    }

    protected void startTimer() {
        dataserviceRpcService.startTimerForChannel(channelId, channelName);
    }

    protected void pauseTimer() {
        dataserviceRpcService.pauseTimerForChannel(channelId, channelName);
    }

    protected UserConfigurationModel getUserConfig() {
        return dataserviceRpcService.getUserConfiguration(channelId);
    }

    protected UserConfigurationModel saveUserConfig(UserConfigurationModel userConfigurationModel) {
        userConfigurationModel.setChannelId(channelId);
        return dataserviceRpcService.saveUserConfiguration(channelId, userConfigurationModel);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        if(channelId.equals(timerEvent.getChannelId())) {
            view.getUI().ifPresent(ui -> ui.access(
                    () -> {
                        view.updateTimerInfo(timerEvent.getCurrentEndTime(), timerEvent.getTimestamp(), timerEvent.getCurrentTimerState());
                        if(timerEvent.getType() == TimerEventType.STATE_CHANGE && timerEvent.getOldTimerState() == TimerState.INITIALIZED) {
                            view.updateTimerInfoStartTime(timerEvent.getTimestamp());
                        }
                    }));
        }
    }

    public void onAttach(AttachEvent event) {
        timerEventService.addEventListener(this);
    }

    public void onDetach(DetachEvent event) {
        timerEventService.removeEventListener(this);
    }
}
