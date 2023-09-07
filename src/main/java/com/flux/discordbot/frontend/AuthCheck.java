package com.flux.discordbot.frontend;

import com.flux.discordbot.frontend.tabs.StatsTab;
import com.flux.discordbot.services.AuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;

public class AuthCheck {
    public void doAuthChecks(final Class<? extends Component> p_clazz) {
        final AuthService.Role role = VaadinSession.getCurrent().getAttribute(AuthService.Role.class);
        if (role == null) {
            UI.getCurrent().navigate("/login");
            return;
        }

        final List<String> routes = role.getAuthorizedRoutes().stream().map(AuthService.AuthorizedRoute::route).toList();

        if (!routes.contains(RouteConfiguration.forSessionScope().getUrl(p_clazz))) {
            UI.getCurrent().navigate("/stats");
        }
    }
}
