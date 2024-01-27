package com.jumani.rutaseg.exception;

public class InvalidRequestOriginException extends ValidationException {
    public InvalidRequestOriginException() {
        super("invalid_request_origin", "request origin is invalid");
    }
}
