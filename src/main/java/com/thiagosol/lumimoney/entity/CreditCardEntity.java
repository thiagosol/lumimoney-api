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
@Table(name = "credit_cards")
public class CreditCardEntity extends PanacheEntityBase {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Integer dueDayOfMonth;

    @Column(nullable = false)
    private Integer closingDayOfMonth;

    @Column(nullable = false)
    private BigDecimal creditLimit;

    @OneToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodEntity paymentMethod;

    @Column(nullable = false)
    private boolean deleted;

    protected CreditCardEntity() {
    }

    public CreditCardEntity(PaymentMethodEntity paymentMethod, Integer dueDayOfMonth, Integer closingDayOfMonth, BigDecimal creditLimit) {
        this.id = UuidCreator.getTimeOrdered();
        this.paymentMethod = paymentMethod;
        this.dueDayOfMonth = dueDayOfMonth;
        this.closingDayOfMonth = closingDayOfMonth;
        this.creditLimit = creditLimit;
        this.deleted = false;
    }

    public UUID getId() {
        return id;
    }

    public Integer getDueDayOfMonth() {
        return dueDayOfMonth;
    }

    public Integer getClosingDayOfMonth() {
        return closingDayOfMonth;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public PaymentMethodEntity getPaymentMethod() {
        return paymentMethod;
    }

    public void delete() {
        this.deleted = true;
    }
}
