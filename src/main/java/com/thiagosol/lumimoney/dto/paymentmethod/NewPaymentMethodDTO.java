package com.thiagosol.lumimoney.dto.paymentmethod;

import com.thiagosol.lumimoney.dto.account.NewAccountDTO;
import com.thiagosol.lumimoney.dto.creditcard.NewCreditCardDTO;
import com.thiagosol.lumimoney.entity.enums.PaymentMethodType;

public record NewPaymentMethodDTO(String name,
                                  PaymentMethodType type,
                                  NewAccountDTO account,
                                  NewCreditCardDTO creditCard) {
}
