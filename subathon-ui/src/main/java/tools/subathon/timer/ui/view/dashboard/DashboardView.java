package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;
import tools.subathon.timer.ui.view.MainLayout;
import tools.subathon.timer.ui.view.dashboard.userconfig.UserConfigurationForm;

import java.util.List;

@PermitAll
@Route(value = "/dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final DashboardPresenter presenter;
    private final AuthenticationContext authContext;

    private final Binder<UserConfigurationModel> binder;
    private UserConfigurationModel userConfigurationModel;

    private Button joinChannelButton;

    @Autowired
    public DashboardView(DashboardPresenter presenter, AuthenticationContext authContext) {
        this.presenter = presenter;
        this.authContext = authContext;
        binder = new BeanValidationBinder<>(UserConfigurationModel.class);
    }

    @PostConstruct
    private void init() {
        presenter.init(this);
    }

    protected void initViewInternal() {
        setSizeFull();

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        OAuth2User auth = authContext.getAuthenticatedUser(DefaultOAuth2User.class).orElse(null);
        if (auth == null) {
            // If not OAuth User (for some reason) we logout and redirect to home.
            // TODO: This should work, but need to test it.
            authContext.logout();
            return;
        }

        userConfigurationModel = presenter.getUserConfig(auth.getAttribute("sub"));
        if(userConfigurationModel == null) {
            userConfigurationModel = new UserConfigurationModel();
        }

        Icon channelJoinedIcon = LumoIcon.CHECKMARK.create();
        channelJoinedIcon.setColor("green");
        channelJoinedIcon.setVisible(false);
        Icon channelFailedIcon = LumoIcon.CROSS.create();
        channelFailedIcon.setColor("red");
        channelFailedIcon.setVisible(false);

        joinChannelButton = new Button("Join channel", event -> {
            if (presenter.joinChannel(auth.getName())) {
                channelJoinedIcon.setVisible(true);
                channelFailedIcon.setVisible(false);
                joinChannelButton.setEnabled(false);
            } else {
                channelJoinedIcon.setVisible(false);
                channelFailedIcon.setVisible(true);
                joinChannelButton.setEnabled(true);
            }
        });

        HorizontalLayout joinChannelBox = new HorizontalLayout(joinChannelButton, channelJoinedIcon, channelFailedIcon);
        joinChannelBox.setAlignItems(Alignment.BASELINE);
        content.add(new HorizontalLayout(createDebugDropdown(), joinChannelBox));
        content.add(createConfigForm(auth.getAttribute("sub")));

        Paragraph footerText = new Paragraph();
        footerText.setText("TWITCH, the TWITCH Logo, the Glitch Logo, and/or TWITCHTV are trademarks of Twitch Interactive, Inc. or its affiliates.");
        VerticalLayout footer = new VerticalLayout();
        footer.setWidthFull();
        footer.setAlignSelf(Alignment.END);
        footer.setAlignItems(Alignment.CENTER);
        footer.setMargin(false);
        footer.setSpacing(false);

        footer.add(new Anchor("https://github.com/pron1x/SubathonTimer", "Source on Github!", AnchorTarget.BLANK), footerText);
        add(content, footer);
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

    private Component createConfigForm(String userId) {
        UserConfigurationForm form = new UserConfigurationForm();
        form.setModel(userConfigurationModel);
        form.setSaveHandler(() -> presenter.saveUserConfig(userId, userConfigurationModel));
        return form;
    }

    private <T, E extends HasValue.ValueChangeEvent<T>> HasValue.ValueChangeListener<HasValue.ValueChangeEvent<T>> createValueCopier(HasValue<E, T> other) {
        return e -> {
            if (e.getValue() != null && other.isEmpty()) {
                other.setValue(e.getValue());
            }
        };
    }


}
