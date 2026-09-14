package tools.subathon.timer.ui.view.points;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Component;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEventDto;
import tools.subathon.timer.ui.service.TimerEventService;
import tools.subathon.timer.ui.service.TimerService;
import tools.subathon.timer.util.interfaces.HasLogger;

@UIScope
@Component
public class PointsPresenter implements TimerEventService.TimerEventListener, HasLogger {

    private final TimerEventService timerEventService;

    private final TimerService timerService;

    private final ValueSignal<Long> pointSignal = new ValueSignal<>(0L);

    private TimerDto timerDto;

    public PointsPresenter(TimerEventService timerEventService, TimerService timerService) {
        this.timerEventService = timerEventService;
        this.timerService = timerService;
    }

    public void initForChannel(String channelId) {
        TimerDto timer = getTimerForChannel(channelId);
        if (timer != null) {
            pointSignal.set(timer.points());
        }
    }

    public TimerDto getTimerForChannel(String channelId) {
        if (timerDto == null || !channelId.equals(timerDto.channelId())) {
            RpcResponse<TimerDto> timerResponse = timerService.getTimerForChannel(channelId);
            timerDto = switch (timerResponse) {
                case RpcResponse.Success<TimerDto> success -> success.body();
                case RpcResponse.Failure<TimerDto> error -> {
                    getLogger().error("Error while fetching timer for channelId {}: {}", channelId, error.errorMessage());
                    yield null;
                }
            };
        }
        return timerDto;
    }

    public void onAttach() {
        timerEventService.addEventListener(this);
    }

    public void onDetach() {
        timerEventService.removeEventListener(this);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEventDto timerEventDto) {
        if (!timerDto.id().equals(timerEventDto.timerId())) {
            return;
        }
        pointSignal.set(timerEventDto.newPoints());
    }

    public Signal<Long> getPointSignal() {
        return pointSignal.asReadonly();
    }
}
