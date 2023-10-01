package com.sokima.executor.script.executor.exception;

public class UnsupportedExecutionMethodException extends RuntimeException {
    public UnsupportedExecutionMethodException() {
    }

    public UnsupportedExecutionMethodException(String message) {
        super(message);
    }

    public UnsupportedExecutionMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedExecutionMethodException(Throwable cause) {
        super(cause);
    }

    public UnsupportedExecutionMethodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
