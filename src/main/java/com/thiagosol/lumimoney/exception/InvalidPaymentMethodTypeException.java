package com.thiagosol.lumimoney.exception;

public class InvalidPaymentMethodTypeException extends BusinessException {
    public InvalidPaymentMethodTypeException() {
        super("Transação não pode ser paga pois é de cartão de crédito");
    }
}
