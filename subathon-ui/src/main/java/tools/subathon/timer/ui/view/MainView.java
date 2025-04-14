package tools.subathon.timer.ui.view;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.ui.service.TimerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.List;

@Route(layout = MainLayout.class)
@AnonymousAllowed
public class MainView extends VerticalLayout implements HasLogger {

    private ComboBox<Timer> timerComboBox;

    @Autowired
    public MainView(TimerService timerService) {
        setSizeFull();
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        getLogger().info("Authenticated user: {}. Principal: {}", auth.getName(), auth.getPrincipal());
        if (auth instanceof OAuth2AuthenticationToken) {
            List<Timer> timers = timerService.getAllActiveTimers();
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

            content.add(new HorizontalLayout(timerComboBox, uptime, timer));
        }
        Paragraph footerText = new Paragraph();
        footerText.setText("TWITCH, the TWITCH Logo, the Glitch Logo, and/or TWITCHTV are trademarks of Twitch Interactive, Inc. or its affiliates.");
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setAlignSelf(Alignment.END);
        footer.setAlignItems(Alignment.CENTER);

        footer.add(footerText);
        add(content, footer);
    }
}
