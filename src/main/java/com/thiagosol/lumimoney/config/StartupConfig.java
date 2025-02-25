package com.thiagosol.lumimoney.config;

import com.thiagosol.lumimoney.entity.UserEntity;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class StartupConfig {

    @ConfigProperty(name = "admin.email")
    String adminEmail;

    @ConfigProperty(name = "admin.password")
    String adminPassword;

    @Transactional
    public void init(@Observes StartupEvent event) {
        UserEntity.createMasterUser(adminEmail, adminPassword);
    }
}
