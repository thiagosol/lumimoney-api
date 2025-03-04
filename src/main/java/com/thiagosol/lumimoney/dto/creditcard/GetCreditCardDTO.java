package com.thiagosol.lumimoney.dto.creditcard;

import com.thiagosol.lumimoney.entity.CreditCardEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record GetCreditCardDTO(UUID id,
                             Integer dueDayOfMonth,
                             Integer closingDayOfMonth,
                             BigDecimal creditLimit) {

    public GetCreditCardDTO(CreditCardEntity creditCardEntity) {
        this(creditCardEntity.getId(),
             creditCardEntity.getDueDayOfMonth(),
             creditCardEntity.getClosingDayOfMonth(),
             creditCardEntity.getCreditLimit());
    }
}
