package com.thiagosol.lumimoney.dto.creditcard;

import java.math.BigDecimal;

public record NewCreditCardDTO(Integer dueDayOfMonth,
                             Integer closingDayOfMonth,
                             BigDecimal creditLimit) {
}
