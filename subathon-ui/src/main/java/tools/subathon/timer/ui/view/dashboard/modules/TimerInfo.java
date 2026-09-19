package tools.subathon.timer.ui.view.dashboard.modules;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NonNull;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimerInfo extends Card {

    private final Badge state = new Badge();
    private final Text startTime = new Text(null);
    private final Text updateTime = new Text(null);
    private final Text endTime = new Text(null);
    private final Text points = new Text(null);

    public TimerInfo(String channelName, String channelId) {
        createContent(channelName, channelId);
    }

    private void createContent(String channelName, String channelId) {
        Span startDescription = new Span("Start time");
        startDescription.getStyle().setFontSize("small");

        Span updateDescription = new Span("Last update");
        updateDescription.getStyle().setFontSize("small");

        Span endDescription = new Span("End time");
        endDescription.getStyle().setFontSize("small");

        setTitle(channelName);
        setSubtitle(channelId);
        setHeaderSuffix(state);

        VerticalLayout pointsLayout = new VerticalLayout(new Span("Subathon points"), points);
        pointsLayout.setPadding(false);
        pointsLayout.setSpacing(false);

        VerticalLayout startLayout = new VerticalLayout(startDescription, startTime);
        startLayout.setPadding(false);
        startLayout.setSpacing(false);
        VerticalLayout updateLayout = new VerticalLayout(updateDescription, updateTime);
        updateLayout.setPadding(false);
        updateLayout.setSpacing(false);
        VerticalLayout endLayout = new VerticalLayout(endDescription, endTime);
        endLayout.setPadding(false);
        endLayout.setSpacing(false);

        add(new VerticalLayout(pointsLayout, startLayout, updateLayout, endLayout));
        setWidth("25em");
    }

    public void bindPoints(Signal<Long> pointsSignal) {
        points.bindText(pointsSignal.map(String::valueOf));
    }

    public void bindStartTime(Signal<Instant> startTimeSignal) {
        startTime.bindText(startTimeSignal.map(this::formatInstant));
    }

    public void bindEndTime(Signal<Instant> endTimeSignal) {
        endTime.bindText(endTimeSignal.map(this::formatInstant));
    }

    public void bindUpdateTime(Signal<Instant> updateTimeSignal) {
        updateTime.bindText(updateTimeSignal.map(this::formatInstant));
    }

    public void bindState(Signal<@NonNull TimerState> timerStateSignal) {
        state.bindText(timerStateSignal.map(TimerState::toString));
        state.bindThemeVariant(BadgeVariant.ERROR, timerStateSignal.map(TimerState.PAUSED::equals));
        state.bindThemeVariant(BadgeVariant.SUCCESS, timerStateSignal.map(TimerState.TICKING::equals));
        state.bindThemeVariant(BadgeVariant.CONTRAST, timerStateSignal.map(TimerState.ENDED::equals));
    }

    private String formatInstant(Instant instant) {
        if(instant == null) {
            return "-";
        }
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"));
    }
}
