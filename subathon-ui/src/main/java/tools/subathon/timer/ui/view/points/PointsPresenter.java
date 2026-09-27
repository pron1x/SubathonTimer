package tools.subathon.timer.ui.view.points;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.spring.annotation.UIScope;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;
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
        timerDto = getTimerForChannel(channelId);
        if (timerDto != null) {
            pointSignal.set(timerDto.points());
        }
    }

    public TimerDto getTimerForChannel(String channelId) {
        TimerDto timer = null;
        try {
            timer = timerService.getTimer(channelId);
        } catch (StatusRuntimeException ex) {
            getLogger().error("gRPC error getting timer for channelId '{}', Status {}", channelId, ex.getStatus(), ex);
        }
        return timer;
    }

    public void onAttach() {
        timerEventService.addEventListener(this);
    }

    public void onDetach() {
        timerEventService.removeEventListener(this);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEventDto timerEventDto) {
        if (timerDto == null || !timerDto.channelId().equals(timerEventDto.channelId())) {
            return;
        }
        pointSignal.set(timerEventDto.newPoints());
    }

    public Signal<Long> getPointSignal() {
        return pointSignal.asReadonly();
    }
}
