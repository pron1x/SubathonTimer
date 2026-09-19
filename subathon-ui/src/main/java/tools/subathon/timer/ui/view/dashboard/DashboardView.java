package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import tools.subathon.timer.datamodel.enums.TimerState;
import tools.subathon.timer.datamodel.user.UserConfigurationDto;
import tools.subathon.timer.ui.view.MainLayout;
import tools.subathon.timer.ui.view.dashboard.modules.TimerInfo;
import tools.subathon.timer.ui.view.dashboard.modules.UserConfigurationForm;

@PermitAll
@Route(value = "/dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final DashboardPresenter presenter;

    private UserConfigurationDto userConfigurationModel;
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

    protected void initViewInternal(String channelName, String channelId) {
        setWidthFull();
        setSpacing(false);

        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.getStyle().set("minHeight", "100vh");

        userConfigurationModel = presenter.getUserConfig();
        if(userConfigurationModel == null) {
            userConfigurationModel = new UserConfigurationDto(null, null,
                    null, null, null,
                    null, null, null,
                    null, null, null, null, null, null, null, null);
        }

        initTimerButton = new Button("Initialize a new timer");
        initTimerButton.addClickListener(_ -> presenter.initializeTimer());
        startTimerButton = new Button("Start");
        startTimerButton.addClickListener(_ -> presenter.startTimer());

        pauseTimerButton = new Button("Pause");
        pauseTimerButton.addClickListener(_ -> presenter.pauseTimer());
        configureTimerControls();

        timerInfoCard = new TimerInfo(channelName, channelId);
        configureTimerInfo();

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

        VerticalLayout timerColumn = new VerticalLayout(timerInfoCard);
        timerColumn.setAlignItems(Alignment.END);

        Component configForm = createConfigForm();
        HorizontalLayout timerDashboard = new HorizontalLayout(
                configForm,
                timerColumn
        );
        timerDashboard.setAlignItems(Alignment.STRETCH);
        timerDashboard.setWidthFull();
        content.add(timerDashboard);

        Paragraph footerText = new Paragraph("TWITCH, the TWITCH Logo, the Glitch Logo, and/or TWITCHTV are trademarks of Twitch Interactive, Inc. or its affiliates.");
        VerticalLayout footer = new VerticalLayout();
        footer.setAlignItems(Alignment.CENTER);

        footer.add(new Anchor("https://github.com/pron1x/SubathonTimer", "Source on Github!", AnchorTarget.BLANK), footerText);
        content.add(footer);
        add(content);
    }

    private Component createConfigForm() {
        UserConfigurationForm form = new UserConfigurationForm();
        form.setModel(userConfigurationModel);
        form.setSaveHandler((model) -> form.setModel(presenter.saveUserConfig(model)));
        return form;
    }

    private void configureTimerInfo() {
        timerInfoCard.bindStartTime(presenter.getTimerStartTimeSignal());
        timerInfoCard.bindUpdateTime(presenter.getTimerUpdateTimeSignal());
        timerInfoCard.bindEndTime(presenter.getTimerEndTimeSignal());
        timerInfoCard.bindState(presenter.getTimerStateSignal());
        timerInfoCard.bindPoints(presenter.getTimerPointsSignal());
    }

    private void configureTimerControls() {
        Signal<TimerState> timerStateSignal = presenter.getTimerStateSignal();

        Signal<Boolean> initTimerButtonEnabledSignal = timerStateSignal.map(state -> TimerState.ENDED.equals(state) || TimerState.UNINITIALIZED.equals(state));
        initTimerButton.bindEnabled(initTimerButtonEnabledSignal);
        initTimerButton.bindThemeVariant(ButtonVariant.PRIMARY, initTimerButtonEnabledSignal);
        initTimerButton.bindVisible(initTimerButtonEnabledSignal);

        Signal<Boolean> startTimerButtonEnabledSignal = timerStateSignal.map(state -> TimerState.INITIALIZED.equals(state) || TimerState.PAUSED.equals(state));
        startTimerButton.bindEnabled(startTimerButtonEnabledSignal);
        startTimerButton.bindThemeVariant(ButtonVariant.PRIMARY, startTimerButtonEnabledSignal);

        Signal<Boolean> pauseTimerButtonEnabledSignal = timerStateSignal.map(TimerState.TICKING::equals);
        pauseTimerButton.bindEnabled(pauseTimerButtonEnabledSignal);
        pauseTimerButton.bindThemeVariant(ButtonVariant.PRIMARY, pauseTimerButtonEnabledSignal);
    }

    protected void showSuccessNotification(String message) {
        Notification notification = Notification.show(message);
        notification.setPosition(Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    protected void showErrorNotification(String message) {
        Notification notification = new Notification();
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Text text = new Text(message);
        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.setAriaLabel("Close");
        closeButton.addClickListener(_ -> notification.close());

        HorizontalLayout layout = new HorizontalLayout(text, closeButton);
        layout.setAlignItems(Alignment.CENTER);

        notification.add(layout);
        notification.open();
    }

}
