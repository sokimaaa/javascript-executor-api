package com.sokima.executor.service;

import com.sokima.executor.model.script.ExecutionStatus;
import com.sokima.executor.model.script.ScriptState;

public interface ScriptRetryService {

    /**
     * Retry possible only for scripts with {@link ExecutionStatus} eq to CANCELLED, COMPLETED, or STOPPED.
     *
     * @param scriptId the long script identifier to retry.
     * @return {@link ScriptState}
     */
    ScriptState retryScript(Long scriptId);
}
