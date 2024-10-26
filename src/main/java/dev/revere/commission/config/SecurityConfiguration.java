package dev.revere.commission.config;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServiceInitListener;
import dev.revere.commission.services.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Remi
 * @project Comission-Discord-Bot-SpringBoot
 * @date 10/25/2024
 */
@Configuration
public class SecurityConfiguration {
    @Bean
    public VaadinServiceInitListener vaadinServiceInitListener(AuthService p_authService) {
        return event -> event.getSource().addUIInitListener(uiEvent -> {
            uiEvent.getUI().addBeforeEnterListener(enterEvent -> {
                if (!enterEvent.getLocation().getPath().equals("login") && !p_authService.isAuthorized()) {
                    enterEvent.rerouteTo("login");
                    UI.getCurrent().navigate("login");
                }
            });
        });
    }
}
