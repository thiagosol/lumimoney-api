package com.thiagosol.lumimoney.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.thiagosol.lumimoney.entity.enums.PaymentMethodType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "payment_methods")
public class PaymentMethodEntity extends PanacheEntityBase {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType type;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private boolean deleted;

    protected PaymentMethodEntity() {
    }

    public PaymentMethodEntity(String name, PaymentMethodType type, UserEntity user) {
        this.id = UuidCreator.getTimeOrdered();
        this.name = name;
        this.type = type;
        this.user = user;
        this.deleted = false;
    }

    public UUID getId() {
        return id;
    }

    public PaymentMethodType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void delete() {
        this.deleted = true;
    }
}

