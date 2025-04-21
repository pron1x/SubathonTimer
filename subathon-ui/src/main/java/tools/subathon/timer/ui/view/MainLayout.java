package tools.subathon.timer.ui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
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

        navLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        navLayout.setWidthFull();
        navLayout.setPadding(true);

        left.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        left.setWidthFull();

        middle.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        middle.setWidthFull();

        right.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        right.setWidthFull();

        H2 title = new H2("Subathon Tools");
        loginDialog = new LoginDialog();
        MenuBar userBar = createUserMenuBar(authContext);

        middle.add(title);
        right.add(userBar);

        navLayout.add(left, middle, right);

        addToNavbar(navLayout);
    }

    private MenuBar createUserMenuBar(AuthenticationContext authContext) {
        MenuBar userBar = new MenuBar();

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
        return userBar;
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
