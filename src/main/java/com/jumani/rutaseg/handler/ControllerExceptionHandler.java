package com.jumani.rutaseg.handler;

import com.jumani.rutaseg.dto.result.Error;
import com.jumani.rutaseg.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("([a-z])([A-Z])");

    @ExceptionHandler(ValidationException.class)
    private ResponseEntity<Error> handleValidationException(ValidationException exception) {
        final Error error = new Error(exception.getCode(), exception.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    private ResponseEntity<Error> handleResourceNotFoundException(NotFoundException exception) {
        final Error error = new Error(exception.getCode(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidRequestOriginException.class)
    private ResponseEntity<Error> handleInvalidRequestOriginException(InvalidRequestOriginException exception) {
        final Error error = new Error(exception.getCode(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    private ResponseEntity<Error> handleUnauthorizedException(UnauthorizedException exception) {
        log.error("this is the unauthorized ex", exception);
        final Error error = new Error(exception.getCode(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    private ResponseEntity<Error> handleForbiddenException(ForbiddenException exception) {
        final Error error = new Error(exception.getCode(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleException(Exception exception) {
        return this.handleKnownException(exception).orElseGet(() -> {
            log.error("Internal error", exception);
            final Error error = new Error("internal_error", exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        });
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    private ResponseEntity<Error> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        final Error Error = new Error(
                "invalid_method", String.format("method %s is not supported", e.getMethod()));
        return ResponseEntity.badRequest().body(Error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    private ResponseEntity<Error> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        final Error Error = new Error(
                "invalid_request", String.format("required header %s is missing", e.getHeaderName()));
        return ResponseEntity.badRequest().body(Error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<Error> handleMissingRequestBody(HttpMessageNotReadableException e) {
        final Error Error = new Error(
                "invalid_request", "required request body is empty or invalid");
        return ResponseEntity.badRequest().body(Error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ErrorWithCauses> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        final List<Cause> causes =
                exception.getBindingResult().getAllErrors().stream().map(this::errorToCause).collect(Collectors.toList());

        final Error error = new Error("invalid_request_body", "some field is invalid");

        return ResponseEntity.badRequest().body(new ErrorWithCauses(error, causes));
    }

    private Optional<ResponseEntity<Error>> handleKnownException(Exception ex) {
        ResponseEntity<Error> response = null;

        if (ex instanceof ValidationException e) response = this.handleValidationException(e);

        if (ex instanceof InvalidRequestOriginException e) response = this.handleInvalidRequestOriginException(e);

        if (ex instanceof UnauthorizedException e) response = this.handleUnauthorizedException(e);

        if (ex instanceof ForbiddenException e) response = this.handleForbiddenException(e);

        return Optional.ofNullable(response);
    }

    private Cause errorToCause(final ObjectError error) {
        final String fieldCamelCase = ((FieldError) error).getField();
        final String fieldName = SNAKE_CASE_PATTERN.matcher(fieldCamelCase).replaceAll("$1_$2").toLowerCase();
        final String errorMessage = error.getDefaultMessage();

        return new Cause(fieldName, errorMessage);
    }

    private record Cause(String code, String message) {

    }

    private record ErrorWithCauses(Error error, List<Cause> causes) {

    }

}
