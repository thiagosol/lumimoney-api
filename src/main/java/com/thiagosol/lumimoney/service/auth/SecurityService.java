package com.thiagosol.lumimoney.service.auth;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

@ApplicationScoped
public class SecurityService {

    @Inject
    KeycloakAuthService keycloakAuthService;

    public static final String ROLE_USER = "lumimoney-user";
    public static final String ROLE_ADMIN = "lumimoney-admin";

    public UUID getAuthenticatedUser() {
        return keycloakAuthService.getAuthenticatedUser();
    }

    public void validateUserAccess() {
        if (!keycloakAuthService.hasRole(ROLE_USER)) {
            throw new ForbiddenException("Usuário não tem permissão para acessar este recurso");
        }
    }
}
