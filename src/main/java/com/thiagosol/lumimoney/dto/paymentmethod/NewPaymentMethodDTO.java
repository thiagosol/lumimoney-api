package com.thiagosol.lumimoney.dto.paymentmethod;

import com.thiagosol.lumimoney.entity.enums.PaymentMethodType;

public record NewPaymentMethodDTO(String name,
                                  PaymentMethodType type) {
}
