package com.thiagosol.lumimoney.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "credit_card_invoices")
public class CreditCardInvoiceEntity extends PanacheEntityBase {

    @Id
    private UUID id;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean isClosed;

    @Column
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodEntity creditCard;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private boolean deleted;

    protected CreditCardInvoiceEntity() {
    }

    public CreditCardInvoiceEntity(LocalDate dueDate, boolean isClosed, BigDecimal totalAmount,
                                   PaymentMethodEntity creditCard, UserEntity user) {
        this.id = UuidCreator.getTimeOrdered();
        this.dueDate = dueDate;
        this.isClosed = isClosed;
        this.totalAmount = totalAmount;
        this.creditCard = creditCard;
        this.user = user;
        this.deleted = false;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public PaymentMethodEntity getCreditCard() {
        return creditCard;
    }

    public boolean isClosed() {
        return isClosed;
    }
}
