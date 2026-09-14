package tools.subathon.timer.ui.view.uptime;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
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

    private final ValueSignal<TimerEventDto> eventDtoSignal = new ValueSignal<>(null);

    private final ValueSignal<Long> startTimeSignal = new ValueSignal<>(0L);
    private final ValueSignal<Long> endTimeSignal = new ValueSignal<>(0L);
    private final ValueSignal<String> timerStateSignal = new ValueSignal<>(null);

    private TimerDto timer;
    private String channelId;

    @Autowired
    public UptimePresenter(TimerService timerService, TimerEventService timerEventService) {
        this.timerService = timerService;
        this.timerEventService = timerEventService;
    }

    public void initForChannel(String channelId) {
        this.channelId = channelId;
        TimerDto timer = getTimerForChannel(channelId);
        if (timer != null) {
            startTimeSignal.set(timer.startTime().toEpochMilli());
            endTimeSignal.set(timer.endTime().toEpochMilli());
            timerStateSignal.set(timer.state().toString());
        }
    }

    public void onAttach(AttachEvent attachEvent) {
        timerEventService.addEventListener(this);
    }

    public void onDetach(DetachEvent detachEvent) {
        timerEventService.removeEventListener(this);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEventDto timerEventDto) {
        if(!timer.id().equals(timerEventDto.timerId()) && !channelId.equals(timerEventDto.channelId())) {
            return;
        }
        if (timerEventDto.oldTimerState() == TimerState.UNINITIALIZED) {
            timer = fetchTimer(timerEventDto.channelId());
        }
        eventDtoSignal.set(timerEventDto);
        startTimeSignal.set(timer.startTime().toEpochMilli());
        endTimeSignal.set(timerEventDto.currentEndTime().toEpochMilli());
        timerStateSignal.set(timerEventDto.currentTimerState().toString());
    }

    // TODO: Handle cases where:
    //          - No previous timer was ever run -> timer is null on init
    //          - Previous timer stopped and new timer is started -> fetch new timer on event?
    private TimerDto getTimerForChannel(String channelId) {
        if(timer == null || !channelId.equals(timer.channelId())) {
            timer = fetchTimer(channelId);
        }
        return timer;
    }

    private TimerDto fetchTimer(String channelId) {
        RpcResponse<TimerDto> timerResponse = timerService.getTimerForChannel(channelId);
        switch (timerResponse) {
            case RpcResponse.Success<TimerDto> success -> {
                return success.body();
            }
            case RpcResponse.Failure<TimerDto> _ -> {
                return null;
            }
        }
    }

    public Signal<Long> getStartTimeSignal() {
        return startTimeSignal.asReadonly();
    }

    public Signal<Long> getEndTimeSignal() {
        return endTimeSignal.asReadonly();
    }

    public Signal<String> getTimerStateSignal() {
        return timerStateSignal.asReadonly();
    }
}






