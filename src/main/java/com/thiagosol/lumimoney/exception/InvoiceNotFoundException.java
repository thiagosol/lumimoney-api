package com.thiagosol.lumimoney.exception;

import jakarta.ws.rs.core.Response;

public class InvoiceNotFoundException extends BusinessException {
    public InvoiceNotFoundException() {
        super("Fatura não encontrada", Response.Status.NOT_FOUND.getStatusCode());
    }
}
