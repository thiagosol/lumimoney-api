package com.thiagosol.lumimoney.dto.auth;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record GoogleUserDTO(String email, String name, String picture) {
}
