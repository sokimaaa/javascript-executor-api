package com.sokima.executor.service;

import com.sokima.executor.model.script.Script;
import com.sokima.executor.model.script.ScriptState;

import java.time.Duration;
import java.util.Optional;

public interface ScriptSubmitterService {

    /**
     * Submit to concurrent execution {@link Script} in specified time.
     *
     * @param script the script context to submit on execution.
     * @param duration the time to wait till submit on execution.
     */
    void submitOnExecutionScheduled(Script script, Duration duration);

    /**
     * Execute script in non-concurrent way.
     *
     * @param script the script context to submit on execution.
     * @return {@link ScriptState}
     */
    ScriptState blockingExecution(Script script);

    /**
     * Submit to concurrent execution {@link Script}.
     *
     * @param script the script context to submit on execution.
     */
    default void submitOnExecution(Script script) {
        submitOnExecutionScheduled(script, Duration.ZERO);
    }

    default Optional<ScriptState> submit(Script script, boolean isBlockingExecution, Optional<String> scheduledTime) {
        if (isBlockingExecution) {
            return Optional.ofNullable(blockingExecution(script));
        }

        if (scheduledTime.isPresent()) {
            submitOnExecutionScheduled(script, Duration.parse(scheduledTime.get()));
        } else {
            submitOnExecution(script);
        }

        return Optional.empty();
    }
}
