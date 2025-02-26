package com.thiagosol.lumimoney.dto.paymentmethod;

import com.thiagosol.lumimoney.entity.PaymentMethodEntity;
import com.thiagosol.lumimoney.entity.enums.PaymentMethodType;

import java.util.UUID;

public record GetPaymentMethodDTO(UUID id,
                                  String name,
                                  PaymentMethodType type) {

    public GetPaymentMethodDTO(PaymentMethodEntity paymentMethodEntity) {
        this(paymentMethodEntity.getId(), paymentMethodEntity.getName(), paymentMethodEntity.getType());
    }
}
