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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.ui.view.MainLayout;

import java.util.List;

@PermitAll
@Route(value = "/dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    private final DashboardPresenter presenter;

    private ComboBox<Timer> timerComboBox;

    private Button joinChannelButton;

    @Autowired
    public DashboardView(DashboardPresenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    private void init() {
        presenter.init(this);
    }

    protected void initViewInternal() {
        setSizeFull();

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken oauth) {
            List<Timer> timers = presenter.getAllActiveTimers();
            Button uptime = new Button("Go to Uptime");
            uptime.addClickListener(event ->
                    getUI().ifPresent(ui -> ui.navigate("uptime/" + timerComboBox.getValue().getChannelId())));
            uptime.setEnabled(false);

            Button timer = new Button("Go to Timer");
            timer.addClickListener(event ->
                    getUI().ifPresent(ui -> ui.navigate("timer/" + timerComboBox.getValue().getChannelId())));
            timer.setEnabled(false);

            timerComboBox = new ComboBox<>();
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

            Icon channelJoinedIcon = LumoIcon.CHECKMARK.create();
            channelJoinedIcon.setColor("green");
            channelJoinedIcon.setVisible(false);
            Icon channelFailedIcon = LumoIcon.CROSS.create();
            channelFailedIcon.setColor("red");
            channelFailedIcon.setVisible(false);


            joinChannelButton = new Button("Join channel", event -> {
                if (presenter.joinChannel(oauth.getName())) {
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
            content.add(new HorizontalLayout(timerComboBox, uptime, timer, joinChannelBox));
            content.add(createConfigForm());
        }
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

    private Component createConfigForm() {
        PasswordField seJwt = new PasswordField("StreamElements JWT Token");
        IntegerField followerSeconds = new IntegerField("Follower");
        IntegerField raiderSeconds = new IntegerField("Raider");
        IntegerField tier1Seconds = new IntegerField("Tier 1");
        IntegerField tier2Seconds = new IntegerField("Tier 2");
        IntegerField tier3Seconds = new IntegerField("Tier 3");
        IntegerField tier1GiftSeconds = new IntegerField("Tier 1 Gift");
        IntegerField tier2GiftSeconds = new IntegerField("Tier 2 Gift");
        IntegerField tier3GiftSeconds = new IntegerField("Tier 3 Gift");
        IntegerField bitsSeconds = new IntegerField("100 bits");
        IntegerField currencySeconds = new IntegerField("1 currency Donation");
        IntegerField initialSeconds = new IntegerField("Initial Timer seconds");
        Button saveButton = new Button("Save");

        tier1Seconds.addValueChangeListener(createValueCopier(tier1GiftSeconds));
        tier2Seconds.addValueChangeListener(createValueCopier(tier2GiftSeconds));
        tier3Seconds.addValueChangeListener(createValueCopier(tier3GiftSeconds));
        bitsSeconds.addValueChangeListener(createValueCopier(currencySeconds));

        FormLayout configForm = new FormLayout();
        configForm.add(seJwt, followerSeconds, raiderSeconds, tier1Seconds, tier1GiftSeconds, tier2Seconds, tier2GiftSeconds,
                tier3Seconds, tier3GiftSeconds, bitsSeconds, currencySeconds, initialSeconds);
        configForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("350px", 2));
        configForm.setWidth("400px");

        configForm.setColspan(seJwt, 2);
        configForm.setColspan(initialSeconds, 2);

        return new VerticalLayout(new H3("Seconds to add for events"), configForm, saveButton);
    }

    private <T, E extends HasValue.ValueChangeEvent<T>> HasValue.ValueChangeListener<HasValue.ValueChangeEvent<T>> createValueCopier(HasValue<E, T> other) {
        return e -> {
            if(e.getValue() != null && other.isEmpty()) {
                other.setValue(e.getValue());
            }
        };
    }


}
