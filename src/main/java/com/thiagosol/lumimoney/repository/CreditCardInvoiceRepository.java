package com.thiagosol.lumimoney.repository;

import com.thiagosol.lumimoney.entity.CreditCardInvoiceEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CreditCardInvoiceRepository implements PanacheRepositoryBase<CreditCardInvoiceEntity, UUID> {
}
