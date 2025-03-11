package com.thiagosol.lumimoney.exception;

import jakarta.ws.rs.core.Response;

public class InvoiceNotBelongToPaymentMethodException extends BusinessException {
    public InvoiceNotBelongToPaymentMethodException() {
        super("A fatura informada não pertence ao método de pagamento selecionado", Response.Status.BAD_REQUEST.getStatusCode());
    }
} 
