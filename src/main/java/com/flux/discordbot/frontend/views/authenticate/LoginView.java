package com.flux.discordbot.frontend.views.authenticate;

import com.flux.discordbot.frontend.components.AuthComponent;
import com.flux.discordbot.services.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Route("/login")
@PageTitle("Login")
public class LoginView extends FlexLayout {
    private final AuthComponent m_authComponent;
    private final AuthService m_authService;
    @Autowired
    public LoginView(final AuthComponent p_authComponent, final AuthService p_authService) {
        m_authComponent = p_authComponent;
        m_authService = p_authService;

        setAlignContent(ContentAlignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        m_authComponent.getSubmitButton().addClickListener(p_buttonClickEvent -> {
            if (m_authComponent.getAuthField().getValue().isEmpty()) {
                final Notification notification = Notification.show("Please enter a auth key");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            final String enteredAuthCode = p_authComponent.getAuthField().getValue();

            try {
                m_authService.authenticate(enteredAuthCode);
                UI.getCurrent().navigate("pending");
            } catch (final AuthService.AuthException p_e) {
                final Notification notification = Notification.show("Invalid auth key");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                log.warn("Failed login attempt from " + VaadinSession.getCurrent().getBrowser().getAddress() + " with key " + enteredAuthCode);
            }
        });

        add(m_authComponent);
    }
}
