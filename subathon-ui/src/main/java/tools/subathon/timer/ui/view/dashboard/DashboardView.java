package tools.subathon.timer.ui.view.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
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

            joinChannelButton = new Button("Join channel", event -> presenter.joinChannel(oauth.getName()));

            content.add(timerComboBox, uptime, timer);
            content.add(joinChannelButton);
        }
        add(content);
    }


}
