package com.sokima.executor.script.manager;

import com.sokima.executor.model.script.ExecutionStatus;
import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.model.search.SearchParamHolder;
import com.sokima.executor.persistence.ExecutorMongoTemplate;
import com.sokima.executor.script.manager.exception.IllegalScriptStateException;
import com.sokima.executor.script.manager.exception.ScriptStateNotFoundException;
import com.sokima.executor.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("mongo")
@ConditionalOnProperty(value = "executor.persistence.storage.type", havingValue = "mongo")
@RequiredArgsConstructor
public class MongoScriptStateManager implements ScriptStateManager {

    private final ExecutorMongoTemplate<ScriptState> executorMongoTemplate;

    @Override
    public Optional<ScriptState> findScriptState(Long scriptId) {
        return executorMongoTemplate.findById(scriptId);
    }

    @Override
    public Optional<ScriptState> findExecutionResult(Long scriptId) {
        return executorMongoTemplate.findById(scriptId).map(ScriptState::executionResultOf);
    }

    @Override
    public List<ScriptState> findScriptStatesByUser(String userId, SearchParamHolder searchParam) {
        return executorMongoTemplate.findBy("userId", userId, searchParam);
    }

    @Override
    @Transactional
    public void changeScriptExecutionStatus(Long scriptId, ExecutionStatus executionStatus) {
        executorMongoTemplate.findById(scriptId).ifPresent(scriptState -> {
            ScriptState updatedScriptState = ScriptState.changeExecutionStatus(scriptState, executionStatus);
            executorMongoTemplate.update(updatedScriptState);
            log.info("Script execution status has been changed for ScriptId[{}].", scriptId);
        });
    }

    @Override
    @Transactional
    public void saveIntermediateScriptState(Long scriptId, ScriptState scriptState) {
        ScriptState scriptStateToUpdate = ScriptState.changeScriptId(scriptId, scriptState);
        executorMongoTemplate.update(scriptStateToUpdate);
        log.info("Intermediate Script State has been updated for ScriptId[{}].", scriptId);
    }

    @Override
    @Transactional
    public void persistScriptStateCompletion(Long scriptId) {
        ScriptState scriptStateToComplete = executorMongoTemplate.findById(scriptId)
                .orElse(ScriptState.empty(scriptId));

        ScriptState finishedScriptState = ScriptState.updateFinishedAt(scriptStateToComplete);
        ScriptState finishedScriptStateWithId = ScriptState.changeScriptId(scriptId, finishedScriptState);
        executorMongoTemplate.update(finishedScriptStateWithId);

        changeScriptExecutionStatus(scriptId, ExecutionStatus.COMPLETED);

        Duration executionTime = finishedScriptStateWithId.executionTime();
        String consoleResult = finishedScriptStateWithId.consoleResult();
        String consoleError = finishedScriptStateWithId.consoleError();

        if (Objects.isNull(consoleError)) {
            consoleResult += "\nExecution finished without major errors.";
        }

        if (Objects.isNull(executionTime)) {
            consoleError += "\nExecution time wasn't written successfully.";
            executionTime = Duration.between(finishedScriptStateWithId.startAt(), finishedScriptStateWithId.finishedAt());
        }

        consoleResult += "\nCompletion of execution is being approved.";

        ScriptState completion = ScriptState.completion(finishedScriptStateWithId, executionTime, consoleResult, consoleError);
        ScriptState completionWithId = ScriptState.changeScriptId(scriptId, completion);
        executorMongoTemplate.update(completionWithId);
        log.info("Completion Script State has been persisted for ScriptId[{}].", scriptId);
    }

    @Override
    public Long generateScriptId() {
        final long newScriptId = IdUtil.generateSimpleId();
        Optional<ScriptState> byId = executorMongoTemplate.findById(newScriptId);
        if (byId.isEmpty()) {
            executorMongoTemplate.insert(ScriptState.empty(newScriptId));
            return newScriptId;
        }

        return generateScriptId();
    }

    @Override
    public void removeScript(Long scriptId) {
        ScriptState scriptState = executorMongoTemplate.findById(scriptId).orElseThrow(() -> {
            log.info("ScriptId[{}] does not exist.", scriptId);
            throw new ScriptStateNotFoundException("Script with " + scriptId + "does not exist.");
        });

        ExecutionStatus executionStatus = scriptState.executionStatus();
        if (!isFinished(executionStatus)) {
            log.info("ScriptId[{}] has Illegal execution status. Script can not be removed now.", scriptId);
            throw new IllegalScriptStateException("Script can not be removed with current status.");
        }

        executorMongoTemplate.delete(scriptState);
    }

    private boolean isFinished(ExecutionStatus executionStatus) {
        return executionStatus.equals(ExecutionStatus.COMPLETED)
                || executionStatus.equals(ExecutionStatus.CANCELED)
                || executionStatus.equals(ExecutionStatus.STOPPED);
    }
}
