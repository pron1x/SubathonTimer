package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.enums.TimerState;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.ui.view.MainLayout;
import tools.subathon.timer.ui.view.dashboard.modules.TimerInfo;
import tools.subathon.timer.ui.view.dashboard.modules.UserConfigurationForm;

import java.time.Instant;
import java.util.List;

@PermitAll
@Route(value = "/dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final DashboardPresenter presenter;

    private UserConfigurationModel userConfigurationModel;
    private TimerInfo timerInfoCard;
    private Button initTimerButton;
    private Button pauseTimerButton;
    private Button startTimerButton;

    @Autowired
    public DashboardView(DashboardPresenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    private void init() {
        presenter.init(this);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        presenter.onAttach(attachEvent);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        presenter.onDetach(detachEvent);
    }

    protected void initViewInternal() {
        setWidthFull();
        setSpacing(false);

        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.getStyle().set("minHeight", "100vh");

        userConfigurationModel = presenter.getUserConfig();
        if(userConfigurationModel == null) {
            userConfigurationModel = new UserConfigurationModel();
        }

        Icon channelJoinedIcon = LumoIcon.CHECKMARK.create();
        channelJoinedIcon.setColor("green");
        channelJoinedIcon.setVisible(false);
        Icon channelFailedIcon = LumoIcon.CROSS.create();
        channelFailedIcon.setColor("red");
        channelFailedIcon.setVisible(false);

        initTimerButton = new Button("Initialize a new timer");
        initTimerButton.addClickListener(event -> presenter.initializeTimer());
        startTimerButton = new Button("Start");
        startTimerButton.addClickListener(event -> presenter.startTimer());

        pauseTimerButton = new Button("Pause");
        pauseTimerButton.addClickListener(event -> presenter.pauseTimer());

        Timer timer = presenter.getTimer();
        timerInfoCard = new TimerInfo(timer);

        VerticalLayout timerControls = new VerticalLayout();
        HorizontalLayout timerStateControls = new HorizontalLayout();
        timerStateControls.setFlexGrow(0.5, startTimerButton);
        timerStateControls.setFlexGrow(0.5, pauseTimerButton);
        timerStateControls.setWidthFull();
        timerStateControls.addToStart(startTimerButton);
        timerStateControls.addToEnd(pauseTimerButton);

        timerControls.add(timerStateControls);
        timerControls.add(initTimerButton);
        initTimerButton.setWidthFull();
        timerInfoCard.addToFooter(timerControls);

        setTimerControlButtonStates(timer.getState());

        VerticalLayout timerColumn = new VerticalLayout(timerInfoCard);
        timerColumn.setSpacing(false);
        timerColumn.setPadding(false);
        timerColumn.setAlignItems(Alignment.END);

        Component configForm = createConfigForm();
        HorizontalLayout timerDashboard = new HorizontalLayout(
                configForm,
                timerColumn
        );
        timerDashboard.setAlignItems(Alignment.STRETCH);
        timerDashboard.setWidthFull();
        timerDashboard.setFlexGrow(1, configForm);
        timerDashboard.setFlexGrow(0, timerColumn);
        content.add(timerDashboard);

        Paragraph footerText = new Paragraph("TWITCH, the TWITCH Logo, the Glitch Logo, and/or TWITCHTV are trademarks of Twitch Interactive, Inc. or its affiliates.");
        VerticalLayout footer = new VerticalLayout();
        footer.setAlignItems(Alignment.CENTER);

        footer.add(new Anchor("https://github.com/pron1x/SubathonTimer", "Source on Github!", AnchorTarget.BLANK), footerText);
        content.add(footer);
        add(content);
    }

    private Component createDebugDropdown() {
        List<Timer> timers = presenter.getAllActiveTimers();

        Button uptime = new Button("Go to Uptime");
        Button timer = new Button("Go to Timer");
        ComboBox<Timer> timerComboBox = new ComboBox<>();

        timerComboBox.setItems(timers);
        timerComboBox.setItemLabelGenerator(t -> t.getChannelName() + "(" + t.getChannelId() + ")");
        timerComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                uptime.setEnabled(true);
                timer.setEnabled(true);
            } else {
                uptime.setEnabled(false);
                timer.setEnabled(false);
            }
        });

        uptime.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("uptime/" + timerComboBox.getValue().getChannelId())));
        uptime.setEnabled(false);

        timer.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("timer/" + timerComboBox.getValue().getChannelId())));
        timer.setEnabled(false);

        return new HorizontalLayout(timerComboBox, uptime, timer);
    }

    private Component createConfigForm() {
        UserConfigurationForm form = new UserConfigurationForm();
        form.setModel(userConfigurationModel);
        form.setSaveHandler(() -> form.setModel(presenter.saveUserConfig(userConfigurationModel)));
        return form;
    }

    protected void updateTimerInfo(Instant endTime, Instant updateTime, TimerState timerState) {
        timerInfoCard.updateEndTime(endTime);
        timerInfoCard.updateUpdateTime(updateTime);
        timerInfoCard.updateTimerState(timerState);
        setTimerControlButtonStates(timerState);
    }

    protected void updateTimerInfoStartTime(Instant startTime) {
        timerInfoCard.updateStartTime(startTime);
    }

    private void setTimerControlButtonStates(TimerState timerState) {
        if(timerState == TimerState.INITIALIZED) {
            startTimerButton.setEnabled(true);
            pauseTimerButton.setEnabled(false);
            initTimerButton.setEnabled(false);
            initTimerButton.setVisible(false);
        } else if(timerState == TimerState.ENDED || timerState == TimerState.UNINITIALIZED) {
            startTimerButton.setEnabled(false);
            pauseTimerButton.setEnabled(false);
            initTimerButton.setEnabled(true);
            initTimerButton.setVisible(true);
        } else {
            startTimerButton.setEnabled(true);
            pauseTimerButton.setEnabled(true);
            initTimerButton.setEnabled(false);
            initTimerButton.setVisible(false);
        }
    }

}
