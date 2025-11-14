package com.achobeta.types.exception;

public class UnauthorizedException extends AppException{

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(401, message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(401, cause);
    }
}
