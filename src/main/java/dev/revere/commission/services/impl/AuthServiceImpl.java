package dev.revere.commission.services.impl;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinSession;
import dev.revere.commission.entities.Account;
import dev.revere.commission.frontend.MainLayout;
import dev.revere.commission.repository.AccountRepository;
import dev.revere.commission.services.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final AccountRepository m_accountRepository;
    private final PasswordEncoder m_passwordEncoder;

    public AuthServiceImpl(AccountRepository p_accountRepository) {
        m_accountRepository = p_accountRepository;
        m_passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void createAccount(String p_username, String p_password, Role p_role) {
        if (m_accountRepository.findByUsername(p_username).isPresent()) {
            log.warn("Account with username {} already exists", p_username);
            return;
        }

        Account account = new Account();
        account.setUsername(p_username);
        account.setPassword(m_passwordEncoder.encode(p_password));
        account.setRole(p_role);

        m_accountRepository.save(account);
    }

    @Override
    public void login(String p_username, String p_password) throws AuthException {
        Account account = m_accountRepository.findByUsername(p_username).orElseThrow(() -> new AuthException("Invalid username or password"));

        if (!m_passwordEncoder.matches(p_password, account.getPassword())) {
            throw new AuthException("Invalid username or password");
        }

        VaadinSession.getCurrent().setAttribute(Role.class, account.getRole());
        createRoutes(account.getRole());

        UI.getCurrent().navigate("dashboard");
    }

    @Override
    public void logout() {
        VaadinSession.getCurrent().close();
        UI.getCurrent().navigate("login");
    }

    @Override
    public boolean isAuthorized() {
        return VaadinSession.getCurrent().getAttribute(Role.class) != null;
    }

    private void createRoutes(Role p_role) {
        RouteConfiguration configuration = RouteConfiguration.forSessionScope();

        configuration.getAvailableRoutes().forEach(p_route -> configuration.removeRoute(p_route.getNavigationTarget()));

        p_role.getAuthorizedRoutes()
                .forEach(p_authorizedRoute -> configuration.setRoute(p_authorizedRoute.route(),
                        p_authorizedRoute.view(),
                        MainLayout.class));
    }
}
