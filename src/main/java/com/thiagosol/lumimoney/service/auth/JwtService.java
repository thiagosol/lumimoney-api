package com.thiagosol.lumimoney.service.auth;

import com.thiagosol.lumimoney.entity.enums.Role;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class JwtService {

    public String generateToken(String email, Role role) {
        return Jwt.issuer("lumimoney.thiagosol.com")
                .subject(email)
                .groups(Set.of(role.name()))
                .expiresAt(System.currentTimeMillis() / 1000 + 3600) // 1h
                .sign();
    }
}

