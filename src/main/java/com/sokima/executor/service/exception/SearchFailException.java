package com.sokima.executor.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class SearchFailException extends RuntimeException {

    @Getter
    private final String id;

    public SearchFailException(String message, String id) {
        super(message);
        this.id = id;
    }
}
