package com.pronixxx.subathon.rest;

import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.service.TimerService;
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
    public Timer timerForChannel(@PathVariable String channelId) {
        return timerService.getTimerForChannel(channelId);
    }

    @GetMapping("/timer/all")
    public List<Timer> getAllTimers() {
        return timerService.getAllActiveTimers();
    }
}
