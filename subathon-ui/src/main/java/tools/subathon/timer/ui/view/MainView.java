package tools.subathon.timer.ui.view;

import tools.subathon.timer.datamodel.Timer;
import tools.subathon.timer.ui.service.TimerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route
public class MainView extends HorizontalLayout {

    private TimerService timerService;
    ComboBox<Timer> timerComboBox;

    @Autowired
    public MainView(TimerService timerService) {
        this.timerService = timerService;
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

        add(timerComboBox, uptime, timer);
    }
}
