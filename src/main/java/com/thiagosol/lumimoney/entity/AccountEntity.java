package com.thiagosol.lumimoney.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity extends PanacheEntityBase {

    @Id
    private UUID id;

    @Column(nullable = false)
    private BigDecimal balance;

    @OneToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodEntity paymentMethod;

    @Column(nullable = false)
    private boolean deleted;

    protected AccountEntity() {
    }

    public AccountEntity(PaymentMethodEntity paymentMethod, BigDecimal initialBalance) {
        this.id = UuidCreator.getTimeOrdered();
        this.paymentMethod = paymentMethod;
        this.balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.deleted = false;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public PaymentMethodEntity getPaymentMethod() {
        return paymentMethod;
    }

    public void updateBalance(BigDecimal newBalance) {
        this.balance = newBalance;
    }

    public void delete() {
        this.deleted = true;
    }
}
