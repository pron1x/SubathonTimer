package tools.subathon.timer.ui.config;

import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.shared.SharedValueSignal;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServerTimeSignal {
    private final SharedValueSignal<Long> serverTimeSignal;

    public ServerTimeSignal() {
        this.serverTimeSignal = new SharedValueSignal<>(System.currentTimeMillis());
    }

    public Signal<Long> getServerTimeSignal() {
        return serverTimeSignal.asReadonly();
    }

    @Scheduled(fixedRate = 30_000)
    void update() {
        serverTimeSignal.set(System.currentTimeMillis());
    }
}
