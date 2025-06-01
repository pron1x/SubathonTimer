package tools.subathon.timer.ui.view;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import tools.subathon.timer.util.interfaces.HasLogger;

@Route(value = "/")
@AnonymousAllowed
public class MainView extends VerticalLayout implements HasLogger {

    @Autowired
    public MainView(AuthenticationContext authContext) {
        setSizeFull();
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setAlignItems(Alignment.CENTER);

        SvgIcon twitchLogo = new SvgIcon("/themes/subathon/icons/glitch_flat_black-ops.svg");
        twitchLogo.setSize("var(--lumo-icon-size-l)");

        Anchor login = new Anchor("/oauth2/authorization/twitch", "Login with Twitch");
        login.setRouterIgnore(true);
        login.addComponentAsFirst(twitchLogo);

        Button dashboardLink = new Button("Dashboard");
        dashboardLink.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("dashboard"));
        });

        authContext.getAuthenticatedUser(OAuth2AuthenticatedPrincipal.class)
                .ifPresentOrElse(user -> {
                            content.add(dashboardLink);},
                        () -> content.add(login));

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
}
