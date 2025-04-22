package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

        HorizontalLayout content = new HorizontalLayout();
        content.setSizeFull();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken oauth) {
            List<Timer> timers = presenter.getAllActiveTimers();
            Button uptime = new Button("Go to Uptime");
            uptime.addClickListener(event -> {
                getUI().ifPresent(ui -> {
                    ui.navigate("uptime/" + timerComboBox.getValue().getChannelId());
                });
            });
            uptime.setEnabled(false);

            Button timer = new Button("Go to Timer");
            timer.addClickListener(event -> {
                getUI().ifPresent(ui -> {
                    ui.navigate("timer/" + timerComboBox.getValue().getChannelId());
                });
            });
            timer.setEnabled(false);

            timerComboBox = new ComboBox<>();
            timerComboBox.setItems(timers);
            timerComboBox.setItemLabelGenerator(t -> t.getChannelName() + "(" + t.getChannelId() + ")");
            timerComboBox.addValueChangeListener(e -> {
                if(e.getValue() != null) {
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
            content.add(timerComboBox, uptime, timer);
            content.add(joinChannelBox);
        }
        add(content);
    }


}
