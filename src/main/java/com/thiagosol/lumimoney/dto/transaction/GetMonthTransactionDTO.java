package com.thiagosol.lumimoney.dto.transaction;

import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.enums.TransactionFrequency;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import com.thiagosol.lumimoney.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GetMonthTransactionDTO(
    UUID id,
    String description,
    BigDecimal amount,
    TransactionType type,
    TransactionFrequency frequency,
    TransactionStatus status,
    Integer installmentNumber,
    Integer totalInstallments,
    UUID recurrenceId,
    UUID paymentMethodId,
    String paymentMethodName,
    LocalDate date,
    PaymentMethodDTO paymentMethod,
    CreditCardInvoiceDTO creditCardInvoice,
    boolean isVirtual
) {
    public GetMonthTransactionDTO(TransactionEntity entity) {
        this(
            entity.getId(),
            entity.getDescription(),
            entity.getAmount(),
            entity.getType(),
            entity.getFrequency(),
            entity.getStatus(),
            entity.getInstallmentNumber(),
            entity.getTotalInstallments(),
            entity.getRecurrenceId(),
            entity.getPaymentMethod().getId(),
            entity.getPaymentMethod().getName(),
            entity.getDate().toLocalDate(),
            new PaymentMethodDTO(entity.getPaymentMethod()),
            entity.getCreditCardInvoice() != null ? new CreditCardInvoiceDTO(entity.getCreditCardInvoice()) : null,
            false
        );
    }

    public GetMonthTransactionDTO(TransactionEntity entity, LocalDate newDate) {
        this(
            entity.getId(),
            entity.getDescription(),
            entity.getAmount(),
            entity.getType(),
            entity.getFrequency(),
            entity.getStatus(),
            entity.getInstallmentNumber(),
            entity.getTotalInstallments(),
            entity.getRecurrenceId(),
            entity.getPaymentMethod().getId(),
            entity.getPaymentMethod().getName(),
            newDate,
            new PaymentMethodDTO(entity.getPaymentMethod()),
            entity.getCreditCardInvoice() != null ? new CreditCardInvoiceDTO(entity.getCreditCardInvoice()) : null,
            true
        );
    }

    public static GetMonthTransactionDTO fromCreditCardInvoice(UUID creditCardId, String cardName, BigDecimal amount, LocalDate dueDate, TransactionStatus status) {
        return new GetMonthTransactionDTO(
            UUID.randomUUID(),
            "Fatura " + cardName,
            amount,
            TransactionType.EXPENSE,
            TransactionFrequency.UNITARY,
            status,
            null,
            null,
            null,
            creditCardId,
            cardName,
            dueDate,
            null,
            new CreditCardInvoiceDTO(creditCardId, dueDate, amount),
            true
        );
    }

    public record PaymentMethodDTO(
        UUID id,
        String name,
        String type
    ) {
        public PaymentMethodDTO(com.thiagosol.lumimoney.entity.PaymentMethodEntity entity) {
            this(entity.getId(), entity.getName(), entity.getType().name());
        }
    }

    public record CreditCardInvoiceDTO(
        UUID creditCardId,
        LocalDate dueDate,
        BigDecimal amount
    ) {
        public CreditCardInvoiceDTO(com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity entity) {
            this(entity.getCreditCard().getId(), entity.getDueDate(), entity.getTotalAmount());
        }
    }
} 
