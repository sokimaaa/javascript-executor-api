package com.sokima.executor.service.v1;

import com.sokima.executor.model.script.ExecutionStatus;
import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.script.executor.CodeExecutor;
import com.sokima.executor.script.executor.exception.ExecutionCancellationException;
import com.sokima.executor.script.manager.ScriptStateManager;
import com.sokima.executor.service.ScriptStopperService;
import com.sokima.executor.service.exception.StopFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Qualifier("scriptStopperServiceV1")
@RequiredArgsConstructor
public class ScriptStopperServiceV1 implements ScriptStopperService {

    private static final int INSTANT_RETRY_NUMBER = 1;
    private static final int RETRY_CANCELLATION_COUNT = 3;

    private final CodeExecutor codeExecutor;
    private final ScriptStateManager scriptStateManager;

    @Override
    public ScriptState stopExecution(Long scriptId, boolean isForced) {
        try {
            stopExecutionWithRetry(scriptId, isForced, INSTANT_RETRY_NUMBER);

            if (!codeExecutor.isRunning(scriptId)) {
                scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.STOPPED);
                log.info("ScriptId[{}] was stopped.", scriptId);
            }

            return scriptStateManager.findScriptState(scriptId).orElseThrow(() -> new StopFailException("ScriptId[" + scriptId + "] does not exist."));
        } catch (RuntimeException ex) {
            throw new StopFailException(ex.getMessage(), ex);
        }
    }

    private void stopExecutionWithRetry(Long scriptId, boolean isForced, int retryCount) {
        try {
            codeExecutor.cancelExecution(scriptId, isForced);
        } catch (ExecutionCancellationException ex) {
            log.info("Fail to stop execution ScriptId[{}] by Exception[{}] with RetryCount[{}].", scriptId, ex.getMessage(), retryCount);
            if (retryCount > RETRY_CANCELLATION_COUNT) {
                log.error("Fail to stop execution ScriptId[{}] after {} tries.", scriptId, retryCount);
                throw ex;
            }
            stopExecutionWithRetry(scriptId, isForced, ++retryCount);
        }
    }
}
