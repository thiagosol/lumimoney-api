package com.thiagosol.lumimoney.exception;

public class InvoiceAlreadyPaidException extends BusinessException {
    public InvoiceAlreadyPaidException() {
        super("Fatura já está paga");
    }
}
