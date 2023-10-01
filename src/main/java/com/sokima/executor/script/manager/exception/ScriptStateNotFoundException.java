package com.sokima.executor.script.manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ScriptStateNotFoundException extends RuntimeException {
    public ScriptStateNotFoundException() {
    }

    public ScriptStateNotFoundException(String message) {
        super(message);
    }

    public ScriptStateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptStateNotFoundException(Throwable cause) {
        super(cause);
    }

    public ScriptStateNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
