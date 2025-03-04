package com.thiagosol.lumimoney.dto.paymentmethod;

import com.thiagosol.lumimoney.dto.account.GetAccountDTO;
import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardInvoiceDTO;
import com.thiagosol.lumimoney.entity.AccountEntity;
import com.thiagosol.lumimoney.entity.PaymentMethodEntity;
import com.thiagosol.lumimoney.entity.enums.PaymentMethodType;

import java.util.Optional;
import java.util.UUID;

public record GetPaymentMethodDTO(UUID id,
                                  String name,
                                  PaymentMethodType type,
                                  GetCreditCardInvoiceDTO lastOpenInvoice,
                                  GetAccountDTO account) {

    public GetPaymentMethodDTO(PaymentMethodEntity paymentMethodEntity) {
        this(paymentMethodEntity.getId(), paymentMethodEntity.getName(), paymentMethodEntity.getType(), null, null);
    }

    public GetPaymentMethodDTO(PaymentMethodEntity paymentMethodEntity, GetCreditCardInvoiceDTO lastOpenInvoice) {
        this(paymentMethodEntity.getId(), paymentMethodEntity.getName(), paymentMethodEntity.getType(), lastOpenInvoice, null);
    }

    public GetPaymentMethodDTO(PaymentMethodEntity paymentMethodEntity, AccountEntity accountEntity) {
        this(paymentMethodEntity.getId(), 
             paymentMethodEntity.getName(), 
             paymentMethodEntity.getType(), 
             null,
             Optional.ofNullable(accountEntity).map(GetAccountDTO::new).orElse(null));
    }
}
