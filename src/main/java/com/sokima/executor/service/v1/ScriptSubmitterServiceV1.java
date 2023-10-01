package com.sokima.executor.service.v1;

import com.sokima.executor.model.script.Script;
import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.script.executor.CodeExecutor;
import com.sokima.executor.script.manager.ScriptStateManager;
import com.sokima.executor.service.ScriptSubmitterService;
import com.sokima.executor.service.exception.SubmitFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Qualifier("scriptSubmitterServiceV1")
@RequiredArgsConstructor
public class ScriptSubmitterServiceV1 implements ScriptSubmitterService {

    private final CodeExecutor codeExecutor;

    private final ScriptStateManager scriptStateManager;

    private final ScheduledExecutorService scheduledExecutorService;

    @Override
    public void submitOnExecutionScheduled(Script script, Duration duration) {
        if (duration.isNegative()) {
            log.warn("Provided negative duration. Submitting is rejected for User[{}]", script.userId());
            throw new SubmitFailException("Provided negative duration.");
        }

        try {
            if (duration.isZero()) {
                log.info("Submitting code execution task for User[{}]", script.userId());
                codeExecutor.execute(script);
            } else {
                log.info("Submitting scheduled code execution task for User[{}]", script.userId());
                scheduledExecutorService.schedule(
                        () -> codeExecutor.execute(script),
                        duration.toMillis(),
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (RuntimeException e) {
            log.warn("Task was aborted by [{}] exception because of [{}]", e.getClass(), e.getMessage());
            throw new SubmitFailException(e.getMessage(), e);
        }
    }

    @Override
    public ScriptState blockingExecution(Script script) {
        try {
            log.info("Blocking execution is begun..");
            Long scriptId = codeExecutor.execute(script).get();
            return scriptStateManager.findScriptState(scriptId).orElseThrow(() -> {
                throw new SubmitFailException("Error during extracting the result.");
            });
        } catch (InterruptedException e) {
            log.warn("Thread[{}] was interrupted.", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
            throw new SubmitFailException("Thread was interrupted while blocking execution.");
        } catch (ExecutionException e) {
            log.warn("Task was aborted by thrown exception while blocking execution with {}'s script.", script.userId());
            throw new SubmitFailException("Task was aborted by thrown exception while blocking execution.");
        } catch (RuntimeException e) {
            log.warn("Task was aborted by [{}] exception because of [{}]", e.getClass(), e.getMessage());
            throw new SubmitFailException(e.getMessage(), e);
        }
    }
}
