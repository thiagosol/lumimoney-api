package com.thiagosol.lumimoney.repository;

import com.thiagosol.lumimoney.entity.PaymentMethodEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class PaymentMethodRepository implements PanacheRepositoryBase<PaymentMethodEntity, UUID> {
}
