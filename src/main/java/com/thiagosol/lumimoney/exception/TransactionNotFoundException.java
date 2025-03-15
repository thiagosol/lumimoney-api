package com.thiagosol.lumimoney.exception;

public class TransactionNotFoundException extends BusinessException {
    public TransactionNotFoundException() {
        super("Transação não encontrada");
    }
}
