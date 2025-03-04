package com.thiagosol.lumimoney.repository;

import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CreditCardInvoiceRepository implements PanacheRepositoryBase<CreditCardInvoiceEntity, UUID> {
    
    public Optional<CreditCardInvoiceEntity> findLastOpenInvoice(UUID creditCardId, Long userId) {
        return find("creditCard.id = ?1 and user.id = ?2 and isClosed = false and deleted = false order by dueDate desc", 
                creditCardId, userId)
                .firstResultOptional();
    }
}
