package com.thiagosol.lumimoney.dto.account;

import com.thiagosol.lumimoney.entity.AccountEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record GetAccountDTO(UUID id,
                          BigDecimal balance) {

    public GetAccountDTO(AccountEntity accountEntity) {
        this(accountEntity.getId(), accountEntity.getBalance());
    }
}
