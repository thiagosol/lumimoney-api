package com.thiagosol.lumimoney.dto.creditcard;

import com.thiagosol.lumimoney.dto.paymentmethod.GetPaymentMethodDTO;
import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GetCreditCardInvoiceDTO(UUID id,
                                      LocalDate dueDate,
                                      BigDecimal totalAmount,
                                      boolean isClosed,
                                      GetPaymentMethodDTO creditCard) {

    public GetCreditCardInvoiceDTO(CreditCardInvoiceEntity creditCardInvoiceEntity) {
        this(creditCardInvoiceEntity.getId(), creditCardInvoiceEntity.getDueDate(), creditCardInvoiceEntity.getTotalAmount(),
                creditCardInvoiceEntity.isClosed(), new GetPaymentMethodDTO(creditCardInvoiceEntity.getCreditCard()));
    }
}
