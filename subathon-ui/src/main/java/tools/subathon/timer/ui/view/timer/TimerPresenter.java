package tools.subathon.timer.ui.view.timer;

import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.TimerEvent;
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

import java.util.concurrent.CompletableFuture;

@UIScope
@Controller
public class TimerPresenter implements TimerEventListener, HasLogger {

    private final TimerEventService timerEventService;

    private final TimerService timerService;

    private TimerView timerView;

    private Timer timer;
    private CompletableFuture<Void> stage;

    @Autowired
    public TimerPresenter(TimerEventService timerEventService, TimerService timerService) {
        this.timerEventService = timerEventService;
        this.timerService = timerService;
    }

    protected void init(TimerView timerView) {
        this.timerView = timerView;
    }

    public Timer getTimerForChannel(String channelId) {
        if(timer == null || !channelId.equals(timer.getChannelId())) {
            timer = timerService.getTimerForChannel(channelId);
        }
        return timer;
    }

    public void onAttach(AttachEvent event) {
        timerEventService.addEventListener(this);
    }

    public void onDetach(DetachEvent event) {
        timerEventService.removeEventListener(this);
    }

    @Override
    public void handleIncomingTimerEvent(TimerEvent timerEvent) {
        // Filter for relevant timerEvents
        if(timerEvent.getTimerId() != timer.getId()) {
            return;
        }

        timerView.getUI().ifPresent(ui -> ui.access(() -> timerView.updateTimer(timerEvent)));
        if(TimerEventType.TIME_ADDITION.equals(timerEvent.getType())) {
            addTimeAdditionTheme();
        }
    }

    private void setTimeAddedTheme(String theme, boolean remove) {
        timerView.getUI().ifPresent(ui -> ui.access(() -> {
            if(remove) {
                timerView.removeTimerClassName(theme);
            } else {
                timerView.setTimerClassName(theme);
            }
        }));
    }

    private void addTimeAdditionTheme() {
        if(stage != null && !stage.isDone()) {
            getLogger().debug("Stage is still running. Stopping!");
            stage.cancel(true);
        } else {
            setTimeAddedTheme("neon", false);
        }
        stage = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {/*Nothing to do here*/}
        });
        stage.thenRun(() -> setTimeAddedTheme("neon", true));
    }
}
