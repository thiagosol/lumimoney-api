package com.thiagosol.lumimoney.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class EmailAlreadyRegisteredException extends BusinessException {
    public EmailAlreadyRegisteredException() {
        super("Email já registrado", HttpResponseStatus.UNPROCESSABLE_ENTITY.code());
    }
}
