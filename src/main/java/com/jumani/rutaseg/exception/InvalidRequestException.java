package com.jumani.rutaseg.exception;

public class InvalidRequestException extends ValidationException {
    public InvalidRequestException(String message) {
        super("invalid_request", message);
    }
}
