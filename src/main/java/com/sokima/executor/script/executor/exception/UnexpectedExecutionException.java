package com.sokima.executor.script.executor.exception;

public class UnexpectedExecutionException extends RuntimeException {
    public UnexpectedExecutionException() {
    }

    public UnexpectedExecutionException(String message) {
        super(message);
    }

    public UnexpectedExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedExecutionException(Throwable cause) {
        super(cause);
    }
}
