package tools.subathon.timer.ui.view.dashboard.modules;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimerInfo extends Card {

    private Span state;
    private Text startTime;
    private Text updateTime;
    private Text endTime;

    public TimerInfo(TimerDto timer) {
        createContent(timer);
    }

    private void createContent(TimerDto timer) {
        Span startDescription = new Span("Start time");
        startDescription.getStyle().setFontSize("small");

        Span updateDescription = new Span("Last update");
        updateDescription.getStyle().setFontSize("small");

        Span endDescription = new Span("End time");
        endDescription.getStyle().setFontSize("small");

        setHeader(createHeader(timer));
        state = createTimerStateBadge(timer);
        setHeaderSuffix(state);
        startTime = new Text(timer != null ? formatInstant(timer.startTime()) : "-");
        updateTime = new Text(timer != null ? formatInstant(timer.updateTime()) : "-");
        endTime = new Text(timer != null ? formatInstant(timer.endTime()) : "-");

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
        setWidth("20em");
    }

    private static Div createHeader(TimerDto timer) {
        Div header = new Div();
        header.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN_REVERSE,
                LumoUtility.LineHeight.XSMALL
        );

        H2 title = new H2(timer != null ? timer.channelName() : "-");

        Div subtitle = new Div(timer != null ? timer.channelId() : "-");
        subtitle.addClassNames(
                LumoUtility.TextTransform.UPPERCASE,
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY
        );

        header.add(title, subtitle);
        return header;
    }

    private Span createTimerStateBadge(TimerDto timer) {
        Span badge = new Span(timer != null ? timer.state().toString() : TimerState.UNINITIALIZED.toString());
        badge.getElement().getThemeList().add(getTimerStateBadgeTheme(timer != null ? timer.state() : TimerState.UNINITIALIZED));
        return badge;
    }

    public void updateStartTime(Instant startTime) {
        this.startTime.setText(formatInstant(startTime));
    }

    public void updateEndTime(Instant endTime) {
        this.endTime.setText(formatInstant(endTime));
    }

    public void updateUpdateTime(Instant updateTime) {
        this.updateTime.setText(formatInstant(updateTime));
    }

    public void updateTimerState(TimerState timerState) {
        this.state.setText(timerState.toString());
        state.getElement().getThemeList().clear();
        state.getElement().getThemeList().add(getTimerStateBadgeTheme(timerState));
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
