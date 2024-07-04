package com.pronixxx.subathon.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends HorizontalLayout {

    public MainView() {
        setPadding(true);
        setSpacing(true);
        Button uptime = new Button("Go to Uptime");
        uptime.addClickListener(event -> {
            getUI().ifPresent(ui -> {
                ui.navigate("uptime");
            });
        });

        Button timer = new Button("Go to Timer");
        timer.addClickListener(event -> {
            getUI().ifPresent(ui -> {
                ui.navigate("timer");
            });
        });

        add(uptime, timer);
    }
}
