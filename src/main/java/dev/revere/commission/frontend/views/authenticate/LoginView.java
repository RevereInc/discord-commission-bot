package dev.revere.commission.frontend.views.authenticate;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import dev.revere.commission.Constants;
import dev.revere.commission.services.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@PageTitle(Constants.TITLE_LOGIN)
@Route(value = Constants.PATH_LOGIN)
public class LoginView extends FlexLayout {

    @Autowired
    public LoginView(final AuthService p_authService) {
        addClassName("login-view");
        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        final String js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, Lumo.DARK);

        LoginForm loginForm = new LoginForm();
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.addLoginListener(p_event -> {
            try {
                p_authService.login(p_event.getUsername(), p_event.getPassword());
            } catch (AuthService.AuthException p_e) {
                Notification.show(p_e.getMessage(), 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        add(loginForm);
    }
}