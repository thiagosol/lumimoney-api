package com.thiagosol.lumimoney.service.auth;

import com.thiagosol.lumimoney.entity.UserEntity;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SecurityService {

    @Inject
    SecurityIdentity securityIdentity;

    public UserEntity getAuthenticatedUser() {
        String email = securityIdentity.getPrincipal().getName();
        return UserEntity.<UserEntity>find("email", email).firstResult();
    }
}
