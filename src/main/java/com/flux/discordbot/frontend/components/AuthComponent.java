package com.flux.discordbot.frontend.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import org.springframework.stereotype.Component;
@Getter
@Component
public class AuthComponent extends VerticalLayout {

    private final H1 m_header;
    private final TextField m_authField;
    private final Button m_submitButton;

    public AuthComponent() {
        m_header = new H1("Login Flux Commissions CRM");
        m_authField = new TextField("Auth Code");
        m_submitButton = new Button("Login");

        m_authField.setWidthFull();

        m_submitButton.setWidthFull();
        m_submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(m_header, m_authField, m_submitButton);
    }
}
