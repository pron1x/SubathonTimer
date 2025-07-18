package tools.subathon.timer.ui.view.dashboard.modules;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimerInfo extends Card {

    private Timer timer;

    private Span state;
    private Text startTime;
    private Text updateTime;
    private Text endTime;

    public TimerInfo(Timer timer) {
        this.timer = timer;
        createContent();
    }

    public TimerInfo() {
        this(null);
    }

    private void createContent() {
        Span startDescription = new Span("Start time");
        startDescription.getStyle().setFontSize("small");

        Span updateDescription = new Span("Last update");
        updateDescription.getStyle().setFontSize("small");

        Span endDescription = new Span("End time");
        endDescription.getStyle().setFontSize("small");

        if(timer != null) {
            startTime = new Text(formatInstant(timer.getStartTime()));
            updateTime = new Text(formatInstant(timer.getUpdateTime()));
            endTime = new Text(formatInstant(timer.getEndTime()));
        }
        setTitle(new Div(timer != null ? timer.getChannelName() : "-"));
        setSubtitle(new Div(timer != null ? timer.getChannelId() : "-"));
        state = createTimerStateBadge(timer);
        setHeaderSuffix(state);

        VerticalLayout startLayout = new VerticalLayout(startDescription, startTime);
        startLayout.setPadding(false);
        startLayout.setSpacing(false);
        VerticalLayout updateLayout = new VerticalLayout(updateDescription, updateTime);
        updateLayout.setPadding(false);
        updateLayout.setSpacing(false);
        VerticalLayout endLayout = new VerticalLayout(endDescription, endTime);
        endLayout.setPadding(false);
        endLayout.setSpacing(false);

        add(new VerticalLayout(startLayout, updateLayout, endLayout));
    }

    private Span createTimerStateBadge(Timer timer) {
        Span badge = new Span(timer != null ? timer.getState().toString() : TimerState.UNINITIALIZED.toString());
        badge.getElement().getThemeList().add(getTimerStateBadgeTheme(timer != null ? timer.getState() : TimerState.UNINITIALIZED));
        return badge;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
        startTime.setText(formatInstant(timer.getStartTime()));
        endTime.setText(formatInstant(timer.getEndTime()));
        state.setText(timer.getState().toString());
    }

    private String getTimerStateBadgeTheme(TimerState state) {
        return switch (state) {
            case UNINITIALIZED, INITIALIZED -> "badge";
            case PAUSED -> "badge error";
            case TICKING -> "badge success";
            case ENDED -> "badge contrast";
        };
    }

    private String formatInstant(Instant instant) {
        if(instant == null) {
            return "-";
        }
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
