package tools.subathon.timer.ui.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.RpcStatus;
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
        RpcResponse<TimerDto> timerResponse = timerService.getTimerForChannel(broadcasterUserId);
        return switch(timerResponse) {
            case RpcResponse.Success<TimerDto> success -> ResponseEntity.ok(success.body());
            case RpcResponse.Failure<TimerDto> error -> {
                if (error.statusCode() == RpcStatus.NOT_FOUND) {
                    yield ResponseEntity.notFound().build();
                }
                yield ResponseEntity.internalServerError().build();
            }
        };
    }
}
