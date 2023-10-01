package com.sokima.executor.util.exception;

public class IllegalInstantiationException extends RuntimeException {
    public IllegalInstantiationException() {
    }

    public IllegalInstantiationException(String message) {
        super(message);
    }

    public IllegalInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalInstantiationException(Throwable cause) {
        super(cause);
    }

    public IllegalInstantiationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
