package com.thiagosol.lumimoney.exception;

public class TransactionAlreadyPaidException extends BusinessException {
    public TransactionAlreadyPaidException() {
        super("Transação já está paga");
    }
}
