package tools.subathon.timer.ui;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Push
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
public class SubathonUiApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(SubathonUiApplication.class, args);
    }
}
