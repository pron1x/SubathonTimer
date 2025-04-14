package tools.subathon.timer.ui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

public class MainLayout extends AppLayout {

    private final transient AuthenticationContext authContext;
    private final LoginDialog loginDialog;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;

        HorizontalLayout navLayout = new HorizontalLayout();
        HorizontalLayout left = new HorizontalLayout();
        HorizontalLayout middle = new HorizontalLayout();
        HorizontalLayout right = new HorizontalLayout();

        navLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        navLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        navLayout.setWidthFull();

        left.setAlignItems(FlexComponent.Alignment.START);
        left.setAlignItems(FlexComponent.Alignment.START);
        left.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        //left.setWidthFull();

        middle.setAlignItems(FlexComponent.Alignment.CENTER);
        middle.setAlignSelf(FlexComponent.Alignment.CENTER);
        middle.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        //middle.setWidthFull();

        right.setAlignItems(FlexComponent.Alignment.END);
        right.setAlignSelf(FlexComponent.Alignment.END);
        right.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        //right.setWidthFull();

        H1 title = new H1("Subathon Tools");
        MenuBar userBar = new MenuBar();
        loginDialog = new LoginDialog();

        authContext.getAuthenticatedUser(OAuth2AuthenticatedPrincipal.class)
                .ifPresentOrElse(user -> {
                            MenuItem userItem = userBar.addItem(user.getName());
                            SubMenu userMenu = userItem.getSubMenu();
                            userMenu.addItem("Timer", e ->
                                    getUI().ifPresent(ui -> ui.navigate("timer/" + user.getAttribute("sub"))));
                            userMenu.addItem("Uptime", e ->
                                    getUI().ifPresent(ui -> ui.navigate("uptime/" + user.getAttribute("sub"))));
                        },
                        () -> userBar.addItem("Login", e -> loginDialog.open()));

        left.add(title);
        right.add(userBar);

        navLayout.add(left, middle, right);
//        navLayout.setFlexGrow(1, right);
//        navLayout.setFlexGrow(1, middle);
//        navLayout.setFlexGrow(1, left);

        addToNavbar(navLayout);
    }

    private static class LoginDialog extends Dialog {

        public LoginDialog() {
            SvgIcon twitchLogo = new SvgIcon("/themes/subathon/icons/glitch_flat_black-ops.svg");
            twitchLogo.setSize("var(--lumo-icon-size-l)");

            Anchor login = new Anchor("/oauth2/authorization/twitch", "Login with Twitch");
            login.setRouterIgnore(true);
            login.addComponentAsFirst(twitchLogo);

            add(login);
        }
    }
}
