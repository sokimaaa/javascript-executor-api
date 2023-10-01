package com.sokima.executor.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        BindingResult bindingResult = ex.getBindingResult();
        bindingResult.getFieldErrors().forEach(error -> {
            final String field = error.getField();
            final String message = error.getDefaultMessage();
            errors.put(field, message);
        });

        GeneralExceptionResponse generalExceptionResponse = new GeneralExceptionResponse(
                ex.getMessage(),
                errors,
                (HttpStatus) status,
                Instant.now()
        );
        return new ResponseEntity<>(generalExceptionResponse, status);
    }

    private record GeneralExceptionResponse(
            String message,
            Map<String, String> errors,
            HttpStatus httpStatus,
            Instant raisedAt
    ) {
    }
}
