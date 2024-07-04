package com.pronixxx.subathon.ui.view.uptime;

import com.pronixxx.subathon.ui.component.UptimeClock;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import java.time.ZonedDateTime;

@Route("uptime")
public class UptimeView extends Div {

    private ZonedDateTime startTime;

    public UptimeView() {
        startTime = ZonedDateTime.now();
        UptimeClock clock = new UptimeClock(startTime);
        Div uptimeWrapper = new Div(clock);
        add(uptimeWrapper);
    }
}
