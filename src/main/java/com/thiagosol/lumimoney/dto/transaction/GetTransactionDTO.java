package com.thiagosol.lumimoney.dto.transaction;

import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetTransactionDTO(Long id, String description, BigDecimal amount, TransactionType type,
                                LocalDateTime date) {

    public GetTransactionDTO(TransactionEntity transactionEntity) {
        this(transactionEntity.id, transactionEntity.description, transactionEntity.amount, transactionEntity.type,
                transactionEntity.date);
    }
}
