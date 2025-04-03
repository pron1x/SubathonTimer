package tools.subathon.timer.dataservice.rest;

import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.dataservice.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TimerController {

    private final TimerService timerService;

    @Autowired
    public TimerController(TimerService timerService) {
        this.timerService = timerService;
    }

    @GetMapping("/timer/{channelId}")
    public Timer getLatestTimerForChannel(@PathVariable String channelId) {
        return timerService.getLatestTimerForChannel(channelId);
    }

    @GetMapping("/timer/all")
    public List<Timer> getAllTimers() {
        return timerService.getAllActiveTimers();
    }
}
