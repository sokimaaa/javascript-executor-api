package com.sokima.executor.script.manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
public class IllegalScriptStateException extends RuntimeException {
    public IllegalScriptStateException() {
    }

    public IllegalScriptStateException(String message) {
        super(message);
    }

    public IllegalScriptStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalScriptStateException(Throwable cause) {
        super(cause);
    }

    public IllegalScriptStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
