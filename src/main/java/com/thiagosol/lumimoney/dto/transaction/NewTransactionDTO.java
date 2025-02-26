package com.thiagosol.lumimoney.dto.transaction;

import com.thiagosol.lumimoney.entity.enums.TransactionFrequency;
import com.thiagosol.lumimoney.entity.enums.TransactionStatus;
import com.thiagosol.lumimoney.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record NewTransactionDTO(String description,
                                BigDecimal amount,
                                TransactionType type,
                                TransactionFrequency frequency,
                                TransactionStatus status,
                                Integer totalInstallments,
                                UUID paymentMethod,
                                UUID creditCardInvoice,
                                LocalDateTime date) {
}
