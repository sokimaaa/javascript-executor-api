package com.sokima.executor.script.executor.exception;

public class UnsupportedProgrammingLanguageException extends RuntimeException {
    public UnsupportedProgrammingLanguageException() {
    }

    public UnsupportedProgrammingLanguageException(String message) {
        super(message);
    }

    public UnsupportedProgrammingLanguageException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedProgrammingLanguageException(Throwable cause) {
        super(cause);
    }
}
