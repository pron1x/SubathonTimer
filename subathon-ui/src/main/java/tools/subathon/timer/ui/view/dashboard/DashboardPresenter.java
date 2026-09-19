package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.spring.annotation.UIScope;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.RpcStatus;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.datamodel.enums.TimerState;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.ui.service.TimerEventService;
import tools.subathon.timer.ui.service.TimerEventService.TimerEventListener;
import tools.subathon.timer.ui.service.TimerService;
import tools.subathon.timer.ui.service.UserConfigurationService;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.time.Instant;

@UIScope
@Component
public class DashboardPresenter implements TimerEventListener, HasLogger {

    private final String channelId;
    private final String channelName;
    private final TimerEventService timerEventService;
    private final TimerService timerService;
    private final UserConfigurationService userConfigurationService;
    private DashboardView view;

    private final ValueSignal<Instant> timerStartTimeSignal = new ValueSignal<>(null);
    private final ValueSignal<Instant> timerUpdateTimeSignal = new ValueSignal<>(null);
    private final ValueSignal<Instant> timerEndTimeSignal = new ValueSignal<>(null);
    private final ValueSignal<@NonNull TimerState> timerStateSignal = new ValueSignal<>(TimerState.UNINITIALIZED);
    private final ValueSignal<Long> timerPointsSignal = new ValueSignal<>(0L);

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
        TimerDto timer = getTimer();
        if (timer != null) {
            timerStartTimeSignal.set(timer.startTime());
            timerUpdateTimeSignal.set(timer.updateTime());
            timerEndTimeSignal.set(timer.endTime());
            timerStateSignal.set(timer.state());
            timerPointsSignal.set(timer.points());
        }
        view.initViewInternal(channelName, channelId);
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

    protected UserConfigurationDto getUserConfig() {
        RpcResponse<UserConfigurationDto> configResponse = userConfigurationService.getUserConfiguration(channelId);
        return switch(configResponse) {
            case RpcResponse.Success<UserConfigurationDto> success -> success.body();
            case RpcResponse.Failure<UserConfigurationDto> error -> {
                if(error.statusCode() == RpcStatus.ERROR) {
                    getLogger().error("Error while fetching user configuration for channelId {}: {}", channelId, error.errorMessage());
                    view.showErrorNotification("Error loading channel configuration!");
                }
                yield null;
            }
        };
    }

    protected UserConfigurationDto saveUserConfig(UserConfigurationDto userConfigurationModel) {
        RpcResponse<UserConfigurationDto> response = userConfigurationService.saveUserConfiguration(channelId, UserConfigurationDto.withChannelId(userConfigurationModel, channelId));
        if(response instanceof RpcResponse.Failure<UserConfigurationDto> error) {
            getLogger().error("Error saving user configuration for channelId {}: {}", channelId, error.errorMessage());
            view.showErrorNotification("Could not save channel configuration! Please try again.");
            return userConfigurationModel;
        } else if(response instanceof RpcResponse.Success<UserConfigurationDto>(UserConfigurationDto body)) {
            view.showSuccessNotification("Configuration saved successfully!");
            return body;
        }
        // Should never happen!
        getLogger().warn("Unknown response while saving user configuration for channelId {}: {}", channelId, response);
        return userConfigurationModel;
    }

    @Override
    public void handleIncomingTimerEvent(TimerEventDto timerEventDto) {
        if(channelId.equals(timerEventDto.channelId())) {

            if (timerEventDto.type() == TimerEventType.STATE_CHANGE && timerEventDto.oldTimerState() == TimerState.INITIALIZED) {
                TimerDto timer = getTimer();
                timerStartTimeSignal.set(timer.startTime());
            }

            timerUpdateTimeSignal.set(timerEventDto.timestamp());
            timerEndTimeSignal.set(timerEventDto.currentEndTime());
            timerStateSignal.set(timerEventDto.currentTimerState());
            timerPointsSignal.set(timerEventDto.newPoints());
        }
    }

    public Signal<Instant> getTimerStartTimeSignal() {
        return timerStartTimeSignal.asReadonly();
    }

    public Signal<Instant> getTimerUpdateTimeSignal() {
        return timerUpdateTimeSignal.asReadonly();
    }

    public Signal<Instant> getTimerEndTimeSignal() {
        return timerEndTimeSignal.asReadonly();
    }

    public Signal<@NonNull TimerState> getTimerStateSignal() {
        return timerStateSignal.asReadonly();
    }

    public Signal<Long> getTimerPointsSignal() {
        return timerPointsSignal.asReadonly();
    }

    public void onAttach(AttachEvent event) {
        timerEventService.addEventListener(this);
    }

    public void onDetach(DetachEvent event) {
        timerEventService.removeEventListener(this);
    }
}
