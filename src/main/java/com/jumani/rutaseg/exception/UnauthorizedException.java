package com.jumani.rutaseg.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("invalid or missing session credentials");
    }

    public String getCode() {
        return "unauthorized";
    }
}
