package com.thiagosol.lumimoney.entity;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.thiagosol.lumimoney.entity.enums.Role;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Role role;

    protected UserEntity() {
    }

    public UserEntity(String email) {
        this(email, UUID.randomUUID().toString());
    }

    public UserEntity(String email, String password) {
        this.email = email;
        this.passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        ;
        this.role = Role.USER;
    }

    public UserEntity(String email, String password, Role role) {
        this.email = email;
        this.passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        ;
        this.role = role;
    }

    public static Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public static void createMasterUser(String email, String password) {
        if (findByEmail(email).isEmpty()) {
            UserEntity admin = new UserEntity(email, password, Role.MASTER);
            admin.persist();
        }
    }
}
