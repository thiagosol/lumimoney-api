package com.thiagosol.lumimoney.exception;

import jakarta.ws.rs.core.Response;

public class BusinessException extends RuntimeException {
    private final int statusCode;

    public BusinessException(String message) {
        super(message);
        this.statusCode = Response.Status.BAD_REQUEST.getStatusCode();
    }

    public BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public BusinessException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
