package com.sokima.executor.script.executor.exception;

public class ExecutionCancellationException extends RuntimeException {
    public ExecutionCancellationException() {
    }

    public ExecutionCancellationException(String message) {
        super(message);
    }

    public ExecutionCancellationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutionCancellationException(Throwable cause) {
        super(cause);
    }

    public ExecutionCancellationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
