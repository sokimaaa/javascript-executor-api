package com.sokima.executor.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class SubmitFailException extends RuntimeException {
    public SubmitFailException() {
    }

    public SubmitFailException(String message) {
        super(message);
    }

    public SubmitFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubmitFailException(Throwable cause) {
        super(cause);
    }

    public SubmitFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
