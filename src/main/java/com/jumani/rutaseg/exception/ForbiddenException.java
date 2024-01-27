package com.jumani.rutaseg.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("insufficient privileges to access this resource");
    }

    public String getCode() {
        return "forbidden";
    }
}
