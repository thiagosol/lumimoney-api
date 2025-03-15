package com.thiagosol.lumimoney.exception;

public class TransactionNotPaidException extends BusinessException {
    public TransactionNotPaidException() {
        super("Transação não está paga");
    }
}
