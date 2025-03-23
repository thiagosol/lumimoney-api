package com.thiagosol.lumimoney.service.auth;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class KeycloakAuthService {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    public UUID getAuthenticatedUser() {
        return UUID.fromString(getTokenSubject());
    }

    public String getTokenSubject() {
        return jwt.getSubject();
    }

    public boolean hasRole(String role) {
        return securityIdentity.hasRole(role);
    }

    public String getEmail() {
        String email = jwt.getClaim("email");
        if (email == null) {
            email = jwt.getSubject();
        }
        return email;
    }
} 