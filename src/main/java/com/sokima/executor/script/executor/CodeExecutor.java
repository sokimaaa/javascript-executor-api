package com.sokima.executor.script.executor;

import com.sokima.executor.model.script.ExecutionMethod;
import com.sokima.executor.model.script.Script;
import com.sokima.executor.script.executor.exception.UnsupportedExecutionMethodException;

import java.util.concurrent.CompletableFuture;

public interface CodeExecutor {

    CompletableFuture<Long> executeFunction(Script script);

    CompletableFuture<Long> executeVoidFunction(Script script);

    CompletableFuture<Long> executeConsolePrint(Script script);

    CompletableFuture<Long> executePersistVariable(Script script);

    boolean isRunning(Long scriptId);

    void cancelExecution(Long scriptId, boolean isForced);

    default CompletableFuture<Long> execute(Script script) {
        ExecutionMethod executionMethod = script.executionMethod();

        switch (executionMethod) {
            case FUNCTION_EXECUTION -> {
                return executeFunction(script);
            }
            case VOID_FUNCTION_EXECUTION -> {
                return executeVoidFunction(script);
            }
            case CONSOLE_PRINT_EXECUTION -> {
                return executeConsolePrint(script);
            }
            case VARIABLE_PERSIST_EXECUTION -> {
                return executePersistVariable(script);
            }
            default -> throw new UnsupportedExecutionMethodException("Provided unsupported execution method.");
        }
    }
}
