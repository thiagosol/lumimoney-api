package com.thiagosol.lumimoney.dto.transaction;

import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardInvoiceDTO;
import com.thiagosol.lumimoney.dto.paymentmethod.GetPaymentMethodDTO;
import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.enums.TransactionFrequency;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import com.thiagosol.lumimoney.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record GetTransactionDTO(UUID id,
                                String description,
                                BigDecimal amount,
                                TransactionType type,
                                TransactionFrequency frequency,
                                TransactionStatus status,
                                Integer installmentNumber,
                                Integer totalInstallments,
                                GetPaymentMethodDTO paymentMethod,
                                GetCreditCardInvoiceDTO creditCardInvoice,
                                UUID recurrenceId,
                                LocalDateTime date) {

    public GetTransactionDTO(TransactionEntity transactionEntity) {
        this(transactionEntity.getId(), transactionEntity.getDescription(), transactionEntity.getAmount(),
                transactionEntity.getType(), transactionEntity.getFrequency(), transactionEntity.getStatus(),
                transactionEntity.getInstallmentNumber(), transactionEntity.getTotalInstallments(),
                Optional.ofNullable(transactionEntity.getPaymentMethod()).map(GetPaymentMethodDTO::new).orElse(null),
                Optional.ofNullable(transactionEntity.getCreditCardInvoice()).map(GetCreditCardInvoiceDTO::new).orElse(null),
                transactionEntity.getRecurrenceId(), transactionEntity.getDate());
    }
}
