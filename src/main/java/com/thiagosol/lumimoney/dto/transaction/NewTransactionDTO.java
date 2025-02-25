package com.thiagosol.lumimoney.dto.transaction;

import com.thiagosol.lumimoney.entity.TransactionEntity;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NewTransactionDTO(String description, BigDecimal amount, TransactionType type, LocalDateTime date) {

    public TransactionEntity toEntity(UserEntity userEntity) {
        return new TransactionEntity(description, amount, type, date, userEntity);
    }
}
