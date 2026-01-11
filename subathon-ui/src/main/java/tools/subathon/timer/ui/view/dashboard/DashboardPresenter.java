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
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.RpcStatus;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEvent;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.datamodel.enums.TimerState;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.ui.service.TimerEventService;
import tools.subathon.timer.ui.service.TimerEventService.TimerEventListener;
import tools.subathon.timer.ui.service.TimerService;
import tools.subathon.timer.ui.service.UserConfigurationService;
import tools.subathon.timer.util.interfaces.HasLogger;

@UIScope
@Component
public class DashboardPresenter implements TimerEventListener, HasLogger {

    private final String channelId;
    private final String channelName;
    private final TimerEventService timerEventService;
    private final TimerService timerService;
    private final UserConfigurationService userConfigurationService;
    private DashboardView view;

    @Autowired
    public DashboardPresenter(TimerService timerService, TimerEventService timerEventService, UserConfigurationService userConfigurationService) {
        this.timerService = timerService;
        this.timerEventService = timerEventService;
        this.userConfigurationService = userConfigurationService;

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

    protected TimerDto getTimer() {
        RpcResponse<TimerDto> timerResponse = timerService.getTimerForChannel(channelId);
        return switch(timerResponse) {
            case RpcResponse.Success<TimerDto> success -> success.body();
            case RpcResponse.Failure<TimerDto> error -> {
                if(error.statusCode() == RpcStatus.ERROR) {
                    getLogger().error("Error while fetching timer for channelId {}: {}", channelId, error.errorMessage());
                    view.showErrorNotification("Error loading timer!");
                }
                yield null;
            }
        };
    }

    protected void initializeTimer() {
        timerService.initializeTimerForChannel(channelId, channelName);
    }

    protected void startTimer() {
        timerService.startTimerForChannel(channelId, channelName);
    }

    protected void pauseTimer() {
        timerService.pauseTimerForChannel(channelId, channelName);
    }

    protected UserConfigurationModel getUserConfig() {
        RpcResponse<UserConfigurationModel> configResponse = userConfigurationService.getUserConfiguration(channelId);
        return switch(configResponse) {
            case RpcResponse.Success<UserConfigurationModel> success -> success.body();
            case RpcResponse.Failure<UserConfigurationModel> error -> {
                if(error.statusCode() == RpcStatus.ERROR) {
                    getLogger().error("Error while fetching user configuration for channelId {}: {}", channelId, error.errorMessage());
                    view.showErrorNotification("Error loading channel configuration!");
                }
                yield null;
            }
        };
    }

    protected UserConfigurationModel saveUserConfig(UserConfigurationModel userConfigurationModel) {
        userConfigurationModel.setChannelId(channelId);
        RpcResponse<UserConfigurationModel> response = userConfigurationService.saveUserConfiguration(channelId, userConfigurationModel);
        if(response instanceof RpcResponse.Failure<UserConfigurationModel> error) {
            getLogger().error("Error saving user configuration for channelId {}: {}", channelId, error.errorMessage());
            view.showErrorNotification("Could not save channel configuration! Please try again.");
            return userConfigurationModel;
        } else if(response instanceof RpcResponse.Success<UserConfigurationModel>(UserConfigurationModel body)) {
            view.showSuccessNotification("Configuration saved successfully!");
            return body;
        }
        // Should never happen!
        getLogger().warn("Unknown response while saving user configuration for channelId {}: {}", channelId, response);
        return userConfigurationModel;
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
