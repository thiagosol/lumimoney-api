package com.thiagosol.lumimoney.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class InvalidGoogleTokenException extends BusinessException {
    public InvalidGoogleTokenException() {
        super("Token inválido", HttpResponseStatus.UNAUTHORIZED.code());
    }
}
