package com.sokima.executor.controller.exception;

import com.sokima.executor.script.manager.exception.IllegalScriptStateException;
import com.sokima.executor.script.manager.exception.ScriptStateNotFoundException;
import com.sokima.executor.service.exception.SearchFailException;
import com.sokima.executor.service.exception.StopFailException;
import com.sokima.executor.service.exception.SubmitFailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class ScriptExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ScriptExceptionResponse>  handleRuntimeException(RuntimeException ex) {
        HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
        ScriptExceptionResponse scriptExceptionResponse = new ScriptExceptionResponse(
                ex.getMessage(),
                internalServerError,
                Instant.now()
        );

        log.warn("Unrecognized exception type. Exception was raised by {}.", ex.getMessage());
        return new ResponseEntity<>(scriptExceptionResponse, internalServerError);
    }

    @ExceptionHandler(value = StopFailException.class)
    public ResponseEntity<ScriptExceptionResponse> handleStopException(StopFailException ex) {
        HttpStatus httpStatus;

        if (ex.getMessage().contains("does not exist")) {
            httpStatus = HttpStatus.BAD_REQUEST;
            log.info("Script wasn't stop caused by script does not exist.");
        } else {
            httpStatus = HttpStatus.METHOD_NOT_ALLOWED;
            log.info("Trying to stop script that has Illegal State.");
        }

        ScriptExceptionResponse scriptExceptionResponse = new ScriptExceptionResponse(
                ex.getMessage(),
                httpStatus,
                Instant.now()
        );
        return new ResponseEntity<>(scriptExceptionResponse, httpStatus);
    }

    @ExceptionHandler(value = SubmitFailException.class)
    public ResponseEntity<ScriptExceptionResponse> handleSubmitException(SubmitFailException ex) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ScriptExceptionResponse scriptExceptionResponse = new ScriptExceptionResponse(
                ex.getMessage(),
                badRequest,
                Instant.now()
        );

        log.info("Script wasn't submitted caused by {}.", ex.getMessage());
        return new ResponseEntity<>(scriptExceptionResponse, badRequest);
    }

    @ExceptionHandler(value = SearchFailException.class)
    public ResponseEntity<ScriptExceptionResponse> handleSearchException(SearchFailException ex) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ScriptExceptionResponse scriptExceptionResponse = new ScriptExceptionResponse(
                ex.getMessage(),
                notFound,
                Instant.now()
        );

        log.info("Script[{}] is not found caused by {}.", ex.getId(), ex.getMessage());
        return new ResponseEntity<>(scriptExceptionResponse, notFound);
    }

    @ExceptionHandler(value = {ScriptStateNotFoundException.class, IllegalScriptStateException.class})
    public ResponseEntity<ScriptExceptionResponse> handleRemoveException(RuntimeException ex) {
        HttpStatus httpStatus;
        if (ex instanceof IllegalScriptStateException) {
            httpStatus = HttpStatus.METHOD_NOT_ALLOWED;
            log.info("Removing was aborted caused by script state in illegal state.");
        } else if (ex instanceof ScriptStateNotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
            log.info("Script already removed or don't exist.");
        } else {
            return handleRuntimeException(ex);
        }

        ScriptExceptionResponse scriptExceptionResponse = new ScriptExceptionResponse(
                ex.getMessage(),
                httpStatus,
                Instant.now()
        );
        return new ResponseEntity<>(scriptExceptionResponse, httpStatus);
    }

    private record ScriptExceptionResponse(
            String message,
            HttpStatus httpStatus,
            Instant raisedAt
    ) {
    }
}
