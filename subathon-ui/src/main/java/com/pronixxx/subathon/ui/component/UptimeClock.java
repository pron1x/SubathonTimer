package com.pronixxx.subathon.ui.component;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@JsModule("./src/uptime-clock.ts")
@Tag("uptime-clock")
public class UptimeClock extends Component {

    private ZonedDateTime startTime;

    public UptimeClock(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getElement().callJsFunction("setStartTime", startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
