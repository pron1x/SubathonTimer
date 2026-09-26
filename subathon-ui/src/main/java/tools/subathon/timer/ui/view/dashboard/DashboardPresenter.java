package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.spring.annotation.UIScope;
import io.grpc.StatusRuntimeException;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
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
        TimerDto timer = null;
        try {
            timer = timerService.getTimer(channelId);
        } catch (StatusRuntimeException ex) {
            getLogger().error("gRPC error getting timer for channelId '{}', Status {}", channelId, ex.getStatus(), ex);
            view.showErrorNotification("Error loading timer. Status was: " + ex.getStatus().getCode());
        }
        return timer;
    }

    protected void initializeTimer() {
        try {
            timerService.initialize(channelId, channelName);
        } catch (StatusRuntimeException ex) {
            getLogger().error("gRPC error initializing timer for channel '{}', Status {}", channelId, ex.getStatus(), ex);
            view.showErrorNotification("Error initializing timer, please try again. Status was " + ex.getStatus().getCode());
        }
    }

    protected void startTimer() {
        try {
            timerService.start(channelId, channelName);
        } catch (StatusRuntimeException ex) {
            getLogger().error("gRPC error starting timer for channel '{}', Status {}", channelId, ex.getStatus(), ex);
            view.showErrorNotification("Error starting timer, please try again. Status was " + ex.getStatus().getCode());
        }
    }

    protected void pauseTimer() {
        try {
            timerService.pause(channelId, channelName);
        } catch (StatusRuntimeException ex) {
            getLogger().error("gRPC error pausing timer for channelId '{}', Status {}", channelId, ex.getStatus(), ex);
            view.showErrorNotification("Error pausing timer, please try again. Status was " + ex.getStatus().getCode());
        }
    }

    protected UserConfigurationDto getUserConfig() {
        UserConfigurationDto config = null;
        try {
            config = userConfigurationService.getUserConfiguration(channelId);
        } catch (StatusRuntimeException ex) {
            getLogger().error("gRPC error getting user configuration for channelId '{}', Status {}", channelId, ex.getStatus(), ex);
            view.showErrorNotification("Error loading your configuration, status was: " + ex.getStatus().getCode());
        }
        return config;
    }

    protected UserConfigurationDto saveUserConfig(UserConfigurationDto userConfigurationModel) {
        UserConfigurationDto config = null;
        try {
            config = userConfigurationService.saveUserConfiguration(userConfigurationModel);
            view.showSuccessNotification("Configuration saved successfully!");
        } catch (StatusRuntimeException ex) {
            getLogger().error("gRPC saving user configuration for channelId '{}', Status {}", channelId, ex.getStatus(), ex);
            view.showErrorNotification("Could not save channel configuration! Please try again. Status: " + ex.getStatus().getCode());
        }
        return config;
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
