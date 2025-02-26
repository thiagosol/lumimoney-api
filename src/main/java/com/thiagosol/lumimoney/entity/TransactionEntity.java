package com.thiagosol.lumimoney.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.thiagosol.lumimoney.entity.enums.TransactionFrequency;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import com.thiagosol.lumimoney.entity.enums.TransactionType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity extends PanacheEntityBase {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column
    private Integer installmentNumber;

    @Column
    private Integer totalInstallments;

    @Column
    private UUID recurrenceId;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethodEntity paymentMethod;

    @ManyToOne
    @JoinColumn(name = "credit_card_invoice_id")
    private CreditCardInvoiceEntity creditCardInvoice;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private boolean deleted;

    protected TransactionEntity() {
    }

    public TransactionEntity(String description, BigDecimal amount, TransactionType type, TransactionFrequency frequency,
                             TransactionStatus status, Integer installmentNumber, Integer totalInstallments,
                             UUID recurrenceId, PaymentMethodEntity paymentMethod, CreditCardInvoiceEntity creditCardInvoice,
                             LocalDateTime date, UserEntity user) {
        this.id = UuidCreator.getTimeOrdered();
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.frequency = frequency;
        this.status = status;
        this.installmentNumber = installmentNumber;
        this.totalInstallments = totalInstallments;
        this.recurrenceId = recurrenceId;
        this.paymentMethod = paymentMethod;
        this.creditCardInvoice = creditCardInvoice;
        this.date = date;
        this.user = user;
        this.deleted = false;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionFrequency getFrequency() {
        return frequency;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public PaymentMethodEntity getPaymentMethod() {
        return paymentMethod;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public UUID getRecurrenceId() {
        return recurrenceId;
    }

    public CreditCardInvoiceEntity getCreditCardInvoice() {
        return creditCardInvoice;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void delete() {
        this.deleted = true;
    }
}

