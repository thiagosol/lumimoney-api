package com.thiagosol.lumimoney.exception;

public class InvoiceNotPaidException extends BusinessException {
    public InvoiceNotPaidException() {
        super("Fatura não está paga");
    }
}
