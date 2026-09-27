package tools.subathon.timer.ui.api;

import io.grpc.StatusRuntimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.ui.service.TimerService;

@RestController
@RequestMapping("/api/timers")
public class TimerController {

    private final TimerService timerService;

    public TimerController(TimerService timerService) {
        this.timerService = timerService;
    }

    @GetMapping("/{broadcasterUserId}")
    public ResponseEntity<TimerDto> getTimerForBroadcaster(@PathVariable String broadcasterUserId) {
        try {
            return ResponseEntity.ok(timerService.getTimer(broadcasterUserId));
        } catch (StatusRuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
