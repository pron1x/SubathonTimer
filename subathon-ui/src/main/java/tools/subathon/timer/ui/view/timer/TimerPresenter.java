package tools.subathon.timer.ui.view.timer;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.ui.service.TimerEventService;
import tools.subathon.timer.ui.service.TimerEventService.TimerEventListener;
import tools.subathon.timer.ui.service.TimerService;
import tools.subathon.timer.util.interfaces.HasLogger;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@UIScope
@Controller
public class TimerPresenter implements TimerEventListener, HasLogger {

    private final TimerEventService timerEventService;
    private final TimerService timerService;

    private final ValueSignal<TimerEventDto> eventDtoSignal = new ValueSignal<>(null);

    private final ValueSignal<Long> endTimeSignal = new ValueSignal<>(0L);
    private final ValueSignal<Long> lastUpdateTimeSignal = new ValueSignal<>(0L);
    private final ValueSignal<String> timerStateSignal = new ValueSignal<>("");

    private TimerView timerView;
    private TimerDto timer;

    @Autowired
    public TimerPresenter(TimerEventService timerEventService, TimerService timerService) {
        this.timerEventService = timerEventService;
        this.timerService = timerService;
    }

    protected void init(TimerView timerView) {
        this.timerView = timerView;
    }

    public void initForChannel(String channelId) {
        TimerDto timer = getTimerForChannel(channelId);
        if (timer != null) {
            endTimeSignal.set(timer.endTime().toEpochMilli());
            lastUpdateTimeSignal.set(timer.updateTime().toEpochMilli());
            timerStateSignal.set(timer.state().toString());

            Signal.effect(timerView, ctx -> {
                TimerEventDto event = eventDtoSignal.get();
                if (!ctx.isInitialRun() && TimerEventType.TIME_ADDITION.equals(event.type())) {
                    timerView.getElement().flashClass("time-added");
                }
            });
        }
    }

    public void onAttach(AttachEvent event) {
        timerEventService.addEventListener(this);
    }

    public void onDetach(DetachEvent event) {
        timerEventService.removeEventListener(this);
    }

    public TimerDto getTimerForChannel(String channelId) {
        if(timer == null || !channelId.equals(timer.channelId())) {
            RpcResponse<TimerDto> timerResponse = timerService.getTimerForChannel(channelId);
            timer = switch(timerResponse) {
                case RpcResponse.Success<TimerDto> success -> success.body();
                case RpcResponse.Failure<TimerDto> error -> {
                    getLogger().error("Error while fetching timer for channelId {}: {}", channelId, error.errorMessage());
                    yield null;
                }
            };
        }
        return timer;
    }

    public Signal<Long> getEndTimeSignal() {
        return endTimeSignal.asReadonly();
    }

    public Signal<Long> getLastUpdateTimeSignal() {
        return lastUpdateTimeSignal.asReadonly();
    }

    public Signal<String> getTimerStateSignal() {
        return timerStateSignal.asReadonly();
    }

    @Override
    public void handleIncomingTimerEvent(TimerEventDto timerEventDto) {
        // Filter for relevant timerEvents
        if(!timer.id().equals(timerEventDto.timerId())) {
            return;
        }
        eventDtoSignal.set(timerEventDto);
        endTimeSignal.set(timerEventDto.currentEndTime().toEpochMilli());
        lastUpdateTimeSignal.set(timerEventDto.timestamp().toEpochMilli());
        timerStateSignal.set(timerEventDto.currentTimerState().toString());
    }
}
