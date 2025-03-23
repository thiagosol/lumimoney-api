package com.thiagosol.lumimoney.dto.creditcard;

import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GetCreditCardInvoiceDTO(UUID id,
                                      LocalDate dueDate,
                                      LocalDate closingDate,
                                      BigDecimal totalAmount,
                                      boolean isClosed,
                                      boolean isPaid,
                                      GetCreditCardDTO creditCard) {

    public GetCreditCardInvoiceDTO(CreditCardInvoiceEntity creditCardInvoiceEntity) {
        this(creditCardInvoiceEntity.getId(),
             creditCardInvoiceEntity.getDueDate(),
             creditCardInvoiceEntity.getClosingDate(),
             creditCardInvoiceEntity.getTotalAmount(),
             creditCardInvoiceEntity.isClosed(),
             creditCardInvoiceEntity.isPaid(),
             new GetCreditCardDTO(creditCardInvoiceEntity.getCreditCard()));
    }
}
