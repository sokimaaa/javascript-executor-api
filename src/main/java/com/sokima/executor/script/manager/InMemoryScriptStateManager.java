package com.sokima.executor.script.manager;

import com.sokima.executor.model.script.ExecutionStatus;
import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.model.search.SearchParamHolder;
import com.sokima.executor.script.manager.exception.IllegalScriptStateException;
import com.sokima.executor.script.manager.exception.ScriptStateNotFoundException;
import com.sokima.executor.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@Qualifier("in-memory")
@ConditionalOnProperty(value = "executor.persistence.storage.type", havingValue = "in-memory")
public class InMemoryScriptStateManager implements ScriptStateManager {

    private final Map<Long, ScriptState> inMemoryDatabase = Collections.synchronizedMap(new HashMap<>());

    @Override
    public Optional<ScriptState> findScriptState(Long scriptId) {
        if (inMemoryDatabase.containsKey(scriptId)) {
            return Optional.of(inMemoryDatabase.get(scriptId));
        }

        return Optional.empty();
    }

    @Override
    public Optional<ScriptState> findExecutionResult(Long scriptId) {
        if (inMemoryDatabase.containsKey(scriptId)) {
            return Optional.of(inMemoryDatabase.get(scriptId)).map(ScriptState::executionResultOf);
        }

        return Optional.empty();
    }

    @Override
    public List<ScriptState> findScriptStatesByUser(String userId, SearchParamHolder searchParam) {
        return inMemoryDatabase.values().stream()
                .filter(scriptState -> scriptState.userId().equals(userId))
                .filter(searchParam.predicate())
                .sorted(searchParam.comparator())
                .map(ScriptState::briefOf)
                .skip(searchParam.skip())
                .limit(searchParam.pageSize())
                .toList();
    }

    @Override
    public void changeScriptExecutionStatus(Long scriptId, ExecutionStatus executionStatus) {
        inMemoryDatabase.computeIfPresent(scriptId, (id, scriptState) -> {
            ScriptState updatedScriptState = ScriptState.changeExecutionStatus(scriptState, executionStatus);
            inMemoryDatabase.put(id, updatedScriptState);
            log.info("Script execution status has been changed for ScriptId[{}].", scriptId);
            return updatedScriptState;
        });
    }

    @Override
    public void saveIntermediateScriptState(Long scriptId, ScriptState scriptState) {
        inMemoryDatabase.replace(scriptId, scriptState);
        log.info("Intermediate Script State has been updated for ScriptId[{}].", scriptId);
    }

    @Override
    public void persistScriptStateCompletion(Long scriptId) {
        ScriptState scriptStateToComplete = inMemoryDatabase.getOrDefault(scriptId, ScriptState.empty(scriptId));

        ScriptState finishedScriptState = ScriptState.updateFinishedAt(scriptStateToComplete);
        saveIntermediateScriptState(scriptId, finishedScriptState);

        changeScriptExecutionStatus(scriptId, ExecutionStatus.COMPLETED);

        Duration executionTime = finishedScriptState.executionTime();
        String consoleResult = finishedScriptState.consoleResult();
        String consoleError = finishedScriptState.consoleError();

        if (Objects.isNull(consoleError)) {
            consoleResult += "\nExecution finished without major errors.";
        }

        if (Objects.isNull(executionTime)) {
            consoleError += "\nExecution time wasn't written successfully.";
            executionTime = Duration.between(finishedScriptState.startAt(), finishedScriptState.finishedAt());
        }

        consoleResult += "\nCompletion of execution is being approved.";

        ScriptState completion = ScriptState.completion(finishedScriptState, executionTime, consoleResult, consoleError);
        inMemoryDatabase.replace(scriptId, completion);
        log.info("Completion Script State has been persisted for ScriptId[{}].", scriptId);
    }

    @Override
    public Long generateScriptId() {
        final long newScriptId = IdUtil.generateSimpleId();
        if (!inMemoryDatabase.containsKey(newScriptId)) {
            inMemoryDatabase.put(newScriptId, ScriptState.empty(newScriptId));
            return newScriptId;
        }

        return generateScriptId();
    }

    @Override
    public void removeScript(Long scriptId) {
        ScriptState scriptState = findScriptState(scriptId).orElseThrow(() -> {
            log.info("ScriptId[{}] does not exist.", scriptId);
            throw new ScriptStateNotFoundException("Script with " + scriptId + "does not exist.");
        });

        ExecutionStatus executionStatus = scriptState.executionStatus();
        if (!isFinished(executionStatus)) {
            log.info("ScriptId[{}] has Illegal execution status. Script can not be removed now.", scriptId);
            throw new IllegalScriptStateException("Script can not be removed with current status.");
        }

        inMemoryDatabase.remove(scriptId);
    }

    private boolean isFinished(ExecutionStatus executionStatus) {
        return executionStatus.equals(ExecutionStatus.COMPLETED)
                || executionStatus.equals(ExecutionStatus.CANCELED)
                || executionStatus.equals(ExecutionStatus.STOPPED);
    }
}
