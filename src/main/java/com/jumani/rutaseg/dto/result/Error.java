package com.jumani.rutaseg.dto.result;

public record Error(String code, String message) {
    public String serialize() {
        return String.format("{" +
                "\"code\":\"%s\"," +
                "\"message\":\"%s\"" +
                "}", code, message);
    }
}
