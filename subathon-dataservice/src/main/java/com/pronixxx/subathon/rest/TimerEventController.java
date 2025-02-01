package com.pronixxx.subathon.rest;

import com.pronixxx.subathon.datamodel.Timer;
import com.pronixxx.subathon.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimerEventController {

    private final TimerService timerService;

    @Autowired
    public TimerEventController(TimerService timerService) {
        this.timerService = timerService;
    }

    @GetMapping("/timer/{channelId}")
    public Timer newestTimerEvent(@PathVariable String channelId) {
        return timerService.getTimerForChannel(channelId);
    }
}
