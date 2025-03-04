package com.thiagosol.lumimoney.service;

import com.thiagosol.lumimoney.dto.creditcard.GetCreditCardInvoiceDTO;
import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import com.thiagosol.lumimoney.entity.UserEntity;
import com.thiagosol.lumimoney.repository.CreditCardInvoiceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CreditCardInvoiceService {

    @Inject
    CreditCardInvoiceRepository creditCardInvoiceRepository;

    public List<GetCreditCardInvoiceDTO> getPaymentMethodsByUserAndCreditCard(UserEntity user, UUID creditCardId) {
        return CreditCardInvoiceEntity.<CreditCardInvoiceEntity>list("user = ?1 and creditCard.id = ?2 and deleted = false", user, creditCardId)
                .stream().map(GetCreditCardInvoiceDTO::new)
                .toList();
    }

    public Optional<GetCreditCardInvoiceDTO> getFirstUnpaidInvoice(UUID creditCardId, UserEntity user) {
        return CreditCardInvoiceEntity.<CreditCardInvoiceEntity>find(
                "creditCard.id = ?1 and user = ?2 and isPaid = false and deleted = false order by dueDate asc",
                creditCardId, user)
                .firstResultOptional()
                .map(GetCreditCardInvoiceDTO::new);
    }

    public void payInvoice(UUID invoiceId, UUID creditCardId, UserEntity user) {
        CreditCardInvoiceEntity invoice = CreditCardInvoiceEntity.find(
                "id = ?1 and creditCard.id = ?2 and user = ?3 and deleted = false",
                invoiceId, creditCardId, user).firstResult();
        
        if (invoice != null) {
            invoice.pay();
            invoice.persist();
        }
    }
}
