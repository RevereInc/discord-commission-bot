package com.flux.discordbot.services;

import com.flux.discordbot.frontend.views.ActiveCommissionsView;
import com.flux.discordbot.frontend.views.FinishedCommissionsView;
import com.flux.discordbot.frontend.views.PendingCommissionsView;
import com.vaadin.flow.component.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public interface AuthService {
    String getAuthToken(Role p_role);
    boolean isAuthorized();
    void authenticate(String p_authKey) throws AuthException;

    @Getter
    @AllArgsConstructor
    enum Role {
        ADMIN(
                List.of(
                        new AuthorizedRoute("pending", "Pending Commissions", PendingCommissionsView.class)
                )
        ),
        SUPPORT(
                List.of(
                        new AuthorizedRoute("pending", "Pending Commissions", PendingCommissionsView.class),
                        new AuthorizedRoute("active", "Active Commissions", ActiveCommissionsView.class),
                        new AuthorizedRoute("finished", "Finished Commissions", FinishedCommissionsView.class)
                )
        );

        private final List<AuthorizedRoute> m_authorizedRoutes;
    }

    class AuthException extends Exception {}
    record AuthorizedRoute(String route, String name, Class<? extends Component> view) {}
}
