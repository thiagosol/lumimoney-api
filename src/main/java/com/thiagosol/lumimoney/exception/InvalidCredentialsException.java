package com.thiagosol.lumimoney.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Usuário ou senha inválidos", HttpResponseStatus.UNAUTHORIZED.code());
    }

    public InvalidCredentialsException(Throwable cause) {
        super("Usuário ou senha inválidos", HttpResponseStatus.UNAUTHORIZED.code(), cause);
    }
}
