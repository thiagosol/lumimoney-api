package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardInvoiceDTO;
import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import com.thiagosol.lumimoney.entity.UserEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CreditCardInvoiceService {
    public List<GetCreditCardInvoiceDTO> getPaymentMethodsByUserAndCreditCard(UserEntity user, UUID creditCardId) {
        return CreditCardInvoiceEntity.<CreditCardInvoiceEntity>list("user = ?1 and creditCard.id = ?2", user, creditCardId)
                .stream().map(GetCreditCardInvoiceDTO::new)
                .toList();
    }
}
