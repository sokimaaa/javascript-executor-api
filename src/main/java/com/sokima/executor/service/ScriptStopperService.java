package com.sokima.executor.service;

import com.sokima.executor.model.script.ScriptState;

public interface ScriptStopperService {

    /**
     * Stop execution of script in difference mode.
     * - forced: stop in time, some data could be lost.
     * - safety: save execution state and then stop.
     *
     * @param scriptId the script context to stop execution.
     * @param isForced the boolean that indicates stop format.
     * @return {@link ScriptState}
     */
    ScriptState stopExecution(Long scriptId, boolean isForced);
}
