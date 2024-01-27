package com.jumani.rutaseg.dto.result;

public class Result<T> {
    private final T response;
    private final Error error;

    private Result(T response, Error error) {
        this.response = response;
        this.error = error;
    }

    public static <T> Result<T> response(T response) {
        return new Result<>(response, null);
    }

    public static <T> Result<T> error(String code, String message) {
        final Error error = new Error(code, message);
        return new Result<>(null, error);
    }

    public final boolean isSuccessful() {
        return response != null;
    }

    public T getResponse() {
        if (this.isSuccessful()) {
            return response;
        } else {
            throw new IllegalStateException("cannot invoke getResponse on an error result");
        }
    }

    public Error getError() {
        if (!this.isSuccessful()) {
            return error;
        } else {
            throw new IllegalStateException("cannot invoke getError on an successful result");
        }
    }
}
