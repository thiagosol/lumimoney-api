package com.thiagosol.lumimoney.dto.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MonthTransactionsDTO(
    List<GetMonthTransactionDTO> transactions,
    List<AccountBalanceDTO> accounts
) {
    public record AccountBalanceDTO(
        UUID id,
        String name,
        BigDecimal balance,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal expected
    ) {}
}
