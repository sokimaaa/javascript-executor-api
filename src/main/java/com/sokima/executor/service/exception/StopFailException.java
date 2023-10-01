package com.sokima.executor.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class StopFailException extends RuntimeException {
    public StopFailException() {
    }

    public StopFailException(String message) {
        super(message);
    }

    public StopFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public StopFailException(Throwable cause) {
        super(cause);
    }

    public StopFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
