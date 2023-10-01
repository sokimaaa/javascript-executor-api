package com.sokima.executor.script.manager;

import com.sokima.executor.model.script.ExecutionStatus;
import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.model.search.SearchParamHolder;

import java.util.List;
import java.util.Optional;

public interface ScriptStateManager {

    Optional<ScriptState> findScriptState(Long scriptId);

    Optional<ScriptState> findExecutionResult(Long scriptId);

    List<ScriptState> findScriptStatesByUser(String userId, SearchParamHolder searchParam);

    void changeScriptExecutionStatus(Long scriptId, ExecutionStatus executionStatus);

    void saveIntermediateScriptState(Long scriptId, ScriptState scriptState);

    void persistScriptStateCompletion(Long scriptId);

    Long generateScriptId();

    void removeScript(Long scriptId);
}
