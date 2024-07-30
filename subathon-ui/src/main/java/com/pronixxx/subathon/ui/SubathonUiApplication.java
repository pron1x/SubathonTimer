package com.pronixxx.subathon.ui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Push
@Theme("subathon")
public class SubathonUiApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(SubathonUiApplication.class, args);
    }
}
