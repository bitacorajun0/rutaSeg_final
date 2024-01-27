package com.jumani.rutaseg.exception;

public class NotFoundException extends ValidationException {

    public NotFoundException(String message) {
        super("not_found", message);
    }
}
