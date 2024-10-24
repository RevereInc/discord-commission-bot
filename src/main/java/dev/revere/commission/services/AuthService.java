package dev.revere.commission.services;

import dev.revere.commission.frontend.tabs.CommissionsTab;
import dev.revere.commission.frontend.tabs.FreelancersTab;
import dev.revere.commission.frontend.tabs.PendingTab;
import dev.revere.commission.frontend.tabs.StatsTab;
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
        SUPPORT(
                List.of(
                        new AuthorizedRoute("stats", "Stats", StatsTab.class),
                        new AuthorizedRoute("pending", "Pending Commissions", PendingTab.class)
                )
        ),
        ADMIN(
                List.of(
                        new AuthorizedRoute("stats", "Stats", StatsTab.class),
                        new AuthorizedRoute("pending", "Pending Commissions", PendingTab.class),
                        new AuthorizedRoute("commissions", "Active Commissions", CommissionsTab.class),
                        new AuthorizedRoute("freelancers", "Finished Commissions", FreelancersTab.class)
                )
        );

        private final List<AuthorizedRoute> m_authorizedRoutes;
    }

    class AuthException extends Exception {}
    record AuthorizedRoute(String route, String name, Class<? extends Component> view) {}
}
