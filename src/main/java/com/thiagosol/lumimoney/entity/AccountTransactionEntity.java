package com.thiagosol.lumimoney.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import com.thiagosol.lumimoney.entity.enums.AccountTransactionType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_transactions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"account_id", "transaction_id", "operation_type", "previous_operation_id"}, 
            name = "uk_transaction_operation"),
        @UniqueConstraint(columnNames = {"account_id", "credit_card_invoice_id", "operation_type", "previous_operation_id"}, 
            name = "uk_invoice_operation")
    })
public class AccountTransactionEntity extends PanacheEntityBase {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @ManyToOne
    @JoinColumn(name = "credit_card_invoice_id")
    private CreditCardInvoiceEntity creditCardInvoice;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal previousBalance;

    @Column(nullable = false)
    private BigDecimal currentBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private AccountTransactionType operationType;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "previous_operation_id")
    private AccountTransactionEntity previousOperation;

    @OneToOne(mappedBy = "previousOperation")
    private AccountTransactionEntity nextOperation;

    protected AccountTransactionEntity() {
    }

    public AccountTransactionEntity(AccountEntity account, TransactionEntity transaction,
                                  CreditCardInvoiceEntity creditCardInvoice, BigDecimal amount,
                                  AccountTransactionType operationType, UserEntity user) {
        this.id = UuidCreator.getTimeOrdered();
        this.account = account;
        this.transaction = transaction;
        this.creditCardInvoice = creditCardInvoice;
        this.amount = amount;
        this.previousBalance = account.getBalance();
        this.currentBalance = calculateNewBalance();
        this.operationType = operationType;
        this.date = LocalDateTime.now();
        this.user = user;
    }

    private BigDecimal calculateNewBalance() {
        return switch (operationType) {
            case CREDIT -> previousBalance.add(amount);
            case DEBIT -> previousBalance.subtract(amount);
        };
    }

    public UUID getId() {
        return id;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public TransactionEntity getTransaction() {
        return transaction;
    }

    public CreditCardInvoiceEntity getCreditCardInvoice() {
        return creditCardInvoice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public AccountTransactionType getOperationType() {
        return operationType;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public UserEntity getUser() {
        return user;
    }

    public AccountTransactionEntity getPreviousOperation() {
        return previousOperation;
    }

    public void setPreviousOperation(AccountTransactionEntity previousOperation) {
        this.previousOperation = previousOperation;
        if (previousOperation != null) {
            previousOperation.nextOperation = this;
        }
    }

    public AccountTransactionEntity getNextOperation() {
        return nextOperation;
    }

    public boolean isActive() {
        return nextOperation == null;
    }
}
