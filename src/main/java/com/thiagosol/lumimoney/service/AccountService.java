package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.account.GetAccountDTO;
import com.thiagosol.lumimoney.dto.account.NewAccountDTO;
import com.thiagosol.lumimoney.entity.AccountEntity;
import com.thiagosol.lumimoney.entity.PaymentMethodEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AccountService {

    @Transactional
    public AccountEntity createAccount(PaymentMethodEntity paymentMethod, NewAccountDTO dto) {
        var account = new AccountEntity(paymentMethod, dto != null ? dto.initialBalance() : null);
        account.persist();
        return account;
    }

    public Optional<GetAccountDTO> getAccountByPaymentMethod(UUID paymentMethodId) {
        return AccountEntity.<AccountEntity>find("paymentMethod.id = ?1 and deleted = false", paymentMethodId)
                .firstResultOptional()
                .map(GetAccountDTO::new);
    }

    @Transactional
    public void updateBalance(UUID accountId, UUID paymentMethodId, java.math.BigDecimal newBalance) {
        AccountEntity account = AccountEntity.find("id = ?1 and paymentMethod.id = ?2 and deleted = false",
                accountId, paymentMethodId).firstResult();
        if (account != null) {
            account.updateBalance(newBalance);
            account.persist();
        }
    }
}
