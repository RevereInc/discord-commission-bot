package com.flux.discordbot.services;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    public record AuthKey(String key, long timestamp) {}
    private final Map<Role, AuthKey> m_authKeyMap;

    public AuthServiceImpl() {
        m_authKeyMap = new HashMap<>();

        for (final Role p_role : Role.values()) {
            m_authKeyMap.put(p_role, new AuthKey(generateAuthToken(), System.currentTimeMillis()));
        }
    }

    @Override
    public String getAuthToken(final Role p_role) {
        if (!m_authKeyMap.containsKey(p_role)) {
            regenerateAuthToken(p_role);
            return getAuthToken(p_role);
        }

        final long currentTime = System.currentTimeMillis();
        final AuthKey authKey = m_authKeyMap.get(p_role);

        if (currentTime > authKey.timestamp + (24 * 60 * 60 * 1000)) {
            regenerateAuthToken(p_role);
            return getAuthToken(p_role);
        }

        return authKey.key;
    }

    @Override
    public boolean isAuthorized() {
        final Role role = VaadinSession.getCurrent().getAttribute(Role.class);

        return role == null;
    }

    @Override
    public void authenticate(final String p_authKey) throws AuthException {
        for (final Map.Entry<Role, AuthKey> roleAuthKeyEntry : m_authKeyMap.entrySet()) {
            final Role role = roleAuthKeyEntry.getKey();

            if (getAuthToken(role).equals(p_authKey)) {
                VaadinSession.getCurrent().setAttribute(Role.class, role);
                createRoutes(role);
                return;
            }
        }

        throw new AuthException();
    }

    private void createRoutes(Role p_role) {

    }

    private String generateAuthToken() {
        final String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();

        return Base64.getEncoder().encodeToString(rawToken.getBytes());
    }

    private void regenerateAuthToken(final Role p_role) {
        final String authToken = generateAuthToken();
        final AuthKey authKey = new AuthKey(authToken, System.currentTimeMillis());

        m_authKeyMap.put(p_role, authKey);
    }
}
