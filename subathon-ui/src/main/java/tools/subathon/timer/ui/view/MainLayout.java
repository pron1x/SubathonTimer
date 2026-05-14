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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    private final LoginDialog loginDialog;

    public MainLayout(AuthenticationContext authContext) {
        HorizontalLayout navLayout = new HorizontalLayout();

        navLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        navLayout.setWidthFull();
        navLayout.setPadding(true);

        H2 title = new H2("Subathon Tools");
        loginDialog = new LoginDialog();
        MenuBar userBar = createUserMenuBar(authContext);

        navLayout.addToMiddle(title);
        navLayout.addToEnd(userBar);

        addToNavbar(navLayout);
    }

    private MenuBar createUserMenuBar(AuthenticationContext authContext) {
        MenuBar userBar = new MenuBar();

        authContext.getAuthenticatedUser(OAuth2AuthenticatedPrincipal.class)
                .ifPresentOrElse(user -> {
                            MenuItem userItem = userBar.addItem(user.getName());
                            SubMenu userMenu = userItem.getSubMenu();
                            userMenu.addItem("Timer", _ ->
                                    getUI().ifPresent(ui -> ui.navigate("timer/" + user.getAttribute("sub"))));
                            userMenu.addItem("Uptime", _ ->
                                    getUI().ifPresent(ui -> ui.navigate("uptime/" + user.getAttribute("sub"))));
                            userMenu.addItem("Subathon Points", _ ->
                                    getUI().ifPresent(ui -> ui.navigate("points/" + user.getAttribute("sub"))));
                            userMenu.addItem("Logout", _ ->
                                    getUI().ifPresent(_ -> authContext.logout()));
                        },
                        () -> userBar.addItem("Login", _ -> loginDialog.open()));
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
