package com.thiagosol.lumimoney.exception;

public class InvoiceNotBelongToPaymentMethodException extends BusinessException {
    public InvoiceNotBelongToPaymentMethodException() {
        super("A fatura informada não pertence ao método de pagamento selecionado");
    }
} 
