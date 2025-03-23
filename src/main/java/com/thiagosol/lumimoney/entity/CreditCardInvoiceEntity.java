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
    private LocalDate closingDate;

    @Column(nullable = false)
    private boolean isClosed;

    @Column(nullable = false)
    private boolean isPaid;

    @Column
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "credit_card_id", nullable = false)
    private CreditCardEntity creditCard;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private boolean deleted;

    protected CreditCardInvoiceEntity() {
    }

    public CreditCardInvoiceEntity(LocalDate dueDate, LocalDate closingDate, boolean isClosed, BigDecimal totalAmount,
                                 CreditCardEntity creditCard, UUID userId) {
        this.id = UuidCreator.getTimeOrdered();
        this.dueDate = dueDate;
        this.closingDate = closingDate;
        this.isClosed = isClosed;
        this.isPaid = false;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.creditCard = creditCard;
        this.userId = userId;
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

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public CreditCardEntity getCreditCard() {
        return creditCard;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void close() {
        this.isClosed = true;
    }

    public void pay() {
        this.isPaid = true;
    }

    public void unpay() {
        this.isPaid = false;
    }

    public void delete() {
        this.deleted = true;
    }
}
