package tools.subathon.timer.ui.view.uptime;

import tools.subathon.rpc.RpcResponse;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.datamodel.enums.TimerState;
import tools.subathon.timer.ui.service.TimerEventService;
import tools.subathon.timer.ui.service.TimerEventService.TimerEventListener;
import tools.subathon.timer.ui.service.TimerService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@UIScope
@Controller
public class UptimePresenter implements TimerEventListener {

    private final TimerService timerService;
    private final TimerEventService timerEventService;

    private UptimeView uptimeView;

    private TimerDto timer;

    @Autowired
    public UptimePresenter(TimerService timerService, TimerEventService timerEventService) {
        this.timerService = timerService;
        this.timerEventService = timerEventService;
    }

    protected void init(UptimeView uptimeView) {
        this.uptimeView = uptimeView;
    }

    public void onAttach(AttachEvent attachEvent) {
        timerEventService.addEventListener(this);
    }

    public void onDetach(DetachEvent detachEvent) {
        timerEventService.removeEventListener(this);
    }

    // TODO: Handle cases where:
    //          - No previous timer was ever run -> timer is null on init
    //          - Previous timer stopped and new timer is started -> fetch new timer on event?
    public TimerDto getTimerForChannel(String channelId) {
        if(timer == null || !channelId.equals(timer.channelId())) {
            timer = fetchTimer(channelId);
        }
        return timer;
    }

    @Override
    public void handleIncomingTimerEvent(TimerEventDto timerEventDto) {
        if(!timer.id().equals(timerEventDto.timerId())) {
            return;
        }

        if(timerEventDto.currentTimerState() == TimerState.ENDED ||
                (timerEventDto.currentTimerState() == TimerState.TICKING && timerEventDto.oldTimerState() == TimerState.INITIALIZED)) {
            if(timerEventDto.oldTimerState() == TimerState.INITIALIZED) {
                timer = fetchTimer(timer.channelId()); // Refetch timer with correct start time!
                uptimeView.getUI().ifPresent(ui -> ui.access(() -> uptimeView.setTimer(timer)));
            }
            uptimeView.getUI().ifPresent(ui -> ui.access(() -> uptimeView.updateTimerState(timerEventDto)));
        }
    }

    private TimerDto fetchTimer(String channelId) {
        RpcResponse<TimerDto> timerResponse = timerService.getTimerForChannel(channelId);
        switch (timerResponse) {
            case RpcResponse.Success<TimerDto> success -> {
                return success.body();
            }
            case RpcResponse.Failure<TimerDto> error -> {
                return null;
            }
        }
    }
}






