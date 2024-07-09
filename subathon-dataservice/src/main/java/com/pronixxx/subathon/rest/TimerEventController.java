package com.pronixxx.subathon.rest;

import com.pronixxx.subathon.datamodel.TimerEvent;
import com.pronixxx.subathon.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimerEventController {

    @Autowired
    private TimerService timerService;

    @GetMapping("/timer")
    public TimerEvent newestTimerEvent() {
        return timerService.getLastEvent();
    }
}
