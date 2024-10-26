package dev.revere.commission.services;

import com.vaadin.flow.component.Component;
import dev.revere.commission.frontend.views.AccountManagementView;
import dev.revere.commission.frontend.views.CommissionsView;
import dev.revere.commission.frontend.views.DashboardView;
import dev.revere.commission.frontend.views.FreelancersView;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public interface AuthService {
    void createAccount(String p_username, String p_password, Role p_role);

    void login(String p_username, String p_password) throws AuthException;

    void logout();

    boolean isAuthorized();

    @Getter
    @AllArgsConstructor
    enum Role {
        SUPPORT(
                List.of(
                        new AuthorizedRoute("dashboard", "Dashboard", DashboardView.class),
                        new AuthorizedRoute("commissions", "Commissions", CommissionsView.class)
                )
        ),
        ADMIN(
                List.of(
                        new AuthorizedRoute("dashboard", "Dashboard", DashboardView.class),
                        new AuthorizedRoute("commissions", "Commissions", CommissionsView.class),
                        new AuthorizedRoute("freelancers", "Freelancers", FreelancersView.class)
                )
        ),
        OWNER(
                List.of(
                        new AuthorizedRoute("dashboard", "Dashboard", DashboardView.class),
                        new AuthorizedRoute("commissions", "Commissions", CommissionsView.class),
                        new AuthorizedRoute("freelancers", "Freelancers", FreelancersView.class),
                        new AuthorizedRoute("accounts", "Accounts", AccountManagementView.class)
                )
        );

        private final List<AuthorizedRoute> m_authorizedRoutes;
    }

    record AuthorizedRoute(String route, String name, Class<? extends Component> view) {}

    class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}
