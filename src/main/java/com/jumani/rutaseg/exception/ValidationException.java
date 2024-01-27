package com.jumani.rutaseg.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final String code;
    
    private final String message;

    public ValidationException(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
