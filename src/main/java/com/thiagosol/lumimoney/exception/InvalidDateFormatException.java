package com.thiagosol.lumimoney.exception;

public class InvalidDateFormatException extends BusinessException {
    public InvalidDateFormatException() {
        super("Data inválida. Use o formato YYYY-MM");
    }
}
