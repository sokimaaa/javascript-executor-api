package com.sokima.executor.script.executor.exception;

public class WrongScriptException extends RuntimeException {
    public WrongScriptException() {
    }

    public WrongScriptException(String message) {
        super(message);
    }

    public WrongScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongScriptException(Throwable cause) {
        super(cause);
    }

    public WrongScriptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
