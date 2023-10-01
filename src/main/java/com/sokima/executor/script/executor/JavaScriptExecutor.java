package com.sokima.executor.script.executor;

import com.sokima.executor.holder.configuration.PolyglotContextConfigurationHolder;
import com.sokima.executor.model.script.ExecutionStatus;
import com.sokima.executor.model.script.ProgrammingLanguage;
import com.sokima.executor.model.script.Script;
import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.script.executor.exception.ExecutionCancellationException;
import com.sokima.executor.script.executor.exception.UnsupportedProgrammingLanguageException;
import com.sokima.executor.script.executor.exception.WrongScriptException;
import com.sokima.executor.script.manager.ScriptStateManager;
import com.sokima.executor.util.PolyglotValueUtils;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

@Slf4j
@Primary
@Component
@Qualifier("javascript")
public class JavaScriptExecutor implements CodeExecutor {

    private static final String JS_LANGUAGE_ID = "js";

    private static final String LOG_EXECUTION_CANCELLED_MESSAGE = "Thread-[{}] : Script execution cancelled with ExceptionMessage[{}].";

    private final Map<Long, CompletableFuture<?>> runningJobs = new HashMap<>();

    @Autowired
    private ScriptStateManager scriptStateManager;

    @Autowired
    @Qualifier("commonCompletableFutureExecutorService")
    private ExecutorService executorService;

    @Autowired
    @Qualifier("contextFactory")
    private Function<PolyglotContextConfigurationHolder, Context> contextFactory;

    /**
     * Supply a new prototype {@link Context} bean.
     *
     * @return {@link Context}
     */
    private Context getContext(PolyglotContextConfigurationHolder configuration) {
        return contextFactory.apply(configuration);
    }

    @Override
    public boolean isRunning(Long scriptId) {
        return runningJobs.containsKey(scriptId);
    }

    @Override
    public void cancelExecution(Long scriptId, boolean isForced) {
        CompletableFuture<?> completableFuture = runningJobs.get(scriptId);
        final boolean isCancelled = Objects.nonNull(completableFuture) && completableFuture.cancel(isForced);

        if (!isCancelled) {
            log.warn("Fail to cancel execution with ScriptId[{}]", scriptId);
            throw new ExecutionCancellationException("Fail to cancel execution.");
        }

        cleanUpRunningJob(scriptId);
        log.info("ScriptId[{}] is being cancelled.", scriptId);
    }

    @Override
    public CompletableFuture<Long> executeFunction(Script script) {
        final Long scriptId = scriptStateManager.generateScriptId();
        CompletableFuture<Long> executeFunctionCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ScriptState instantScriptState = ScriptState.of(scriptId, script.userId(), script.programmingLanguage(), script.scriptBody());
            scriptStateManager.saveIntermediateScriptState(scriptId, instantScriptState);

            final String scriptBody = String.format("(%s)", script.scriptBody());

            verifyIsJavascript(script.programmingLanguage());
            scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.RUNNING);
            ScriptState runningScriptState = ScriptState.updateStartAt(
                    scriptStateManager.findScriptState(scriptId).orElseThrow()
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, runningScriptState);

            var contextConfiguration = PolyglotContextConfigurationHolder.create(scriptId);
            Context context = getContext(contextConfiguration);

            Value function = context.eval(JS_LANGUAGE_ID, scriptBody);

            if (!function.canExecute()) {
                log.info("JavaScriptExecutor is cancelled execution because not a function were provided.");
                throw new WrongScriptException("Not a function provided.");
            }

            Value executionValue = function.execute(script.args());

            scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.COMPLETED);
            ScriptState completedScriptState = ScriptState.updateResult(
                    scriptStateManager.findScriptState(scriptId).orElseThrow(),
                    PolyglotValueUtils.retrieveAsString(executionValue)
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, completedScriptState);

            return scriptId;
        }, executorService).whenComplete((res, ex) -> {
            if (Objects.nonNull(ex)) {
                scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.CANCELED);
                ScriptState scriptState = ScriptState.updateError(
                        scriptStateManager.findScriptState(scriptId).orElseThrow(),
                        ex.getMessage()
                );
                scriptStateManager.saveIntermediateScriptState(scriptId, scriptState);
                processException(ex);
            }

            cleanUpRunningJob(scriptId);
            scriptStateManager.persistScriptStateCompletion(scriptId);
        });

        runningJobs.put(scriptId, executeFunctionCompletableFuture);
        return executeFunctionCompletableFuture;
    }

    @Override
    public CompletableFuture<Long> executeVoidFunction(Script script) {
        final Long scriptId = scriptStateManager.generateScriptId();
        CompletableFuture<Long> executeVoidFunctionCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ScriptState instantScriptState = ScriptState.of(scriptId, script.userId(), script.programmingLanguage(), script.scriptBody());
            scriptStateManager.saveIntermediateScriptState(scriptId, instantScriptState);

            final String scriptBody = String.format("(%s)", script.scriptBody());

            verifyIsJavascript(script.programmingLanguage());
            scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.RUNNING);
            ScriptState runningScriptState = ScriptState.updateStartAt(
                    scriptStateManager.findScriptState(scriptId).orElseThrow()
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, runningScriptState);

            var contextConfiguration = PolyglotContextConfigurationHolder.create(scriptId);
            Context context = getContext(contextConfiguration);

            Value function = context.eval(JS_LANGUAGE_ID, scriptBody);

            if (!function.canExecute()) {
                log.info("JavaScriptExecutor is cancelled execution because not a void function were provided.");
                throw new WrongScriptException("Not a function provided.");
            }

            function.executeVoid(script.args());

            scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.COMPLETED);
            ScriptState completedScriptState = ScriptState.updateResult(
                    scriptStateManager.findScriptState(scriptId).orElseThrow(),
                    "Void function is executed."
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, completedScriptState);

            return scriptId;
        }, executorService).whenComplete((res, ex) -> {
            if (Objects.nonNull(ex)) {
                scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.CANCELED);
                ScriptState scriptState = ScriptState.updateError(
                        scriptStateManager.findScriptState(scriptId).orElseThrow(),
                        ex.getMessage()
                );
                scriptStateManager.saveIntermediateScriptState(scriptId, scriptState);
                processException(ex);
            }

            cleanUpRunningJob(scriptId);
            scriptStateManager.persistScriptStateCompletion(scriptId);
        });

        runningJobs.put(scriptId, executeVoidFunctionCompletableFuture);
        return executeVoidFunctionCompletableFuture;
    }

    @Override
    public CompletableFuture<Long> executeConsolePrint(Script script) {
        final Long scriptId = scriptStateManager.generateScriptId();
        CompletableFuture<Long> executeConsolePrintCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ScriptState instantScriptState = ScriptState.of(scriptId, script.userId(), script.programmingLanguage(), script.scriptBody());
            scriptStateManager.saveIntermediateScriptState(scriptId, instantScriptState);

            final String scriptBody = script.scriptBody();

            verifyIsJavascript(script.programmingLanguage());
            scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.RUNNING);
            ScriptState runningScriptState = ScriptState.updateStartAt(
                    scriptStateManager.findScriptState(scriptId).orElseThrow()
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, runningScriptState);

            var contextConfiguration = PolyglotContextConfigurationHolder.create(scriptId, true);
            Context context = getContext(contextConfiguration);

            context.eval(JS_LANGUAGE_ID, scriptBody);

            scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.COMPLETED);
            ScriptState finishedScriptState = scriptStateManager.findScriptState(scriptId).orElseThrow();
            ScriptState completedScriptState = ScriptState.updateResult(
                    finishedScriptState,
                    finishedScriptState.consoleResult() + "Console execution finished.\n"
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, completedScriptState);

            return scriptId;
        }, executorService).whenComplete((res, ex) -> {
            if (Objects.nonNull(ex)) {
                scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.CANCELED);
                ScriptState scriptState = ScriptState.updateError(
                        scriptStateManager.findScriptState(scriptId).orElseThrow(),
                        ex.getMessage()
                );
                scriptStateManager.saveIntermediateScriptState(scriptId, scriptState);
                processException(ex);
            }

            cleanUpRunningJob(scriptId);
            scriptStateManager.persistScriptStateCompletion(scriptId);
        });

        runningJobs.put(scriptId, executeConsolePrintCompletableFuture);
        return executeConsolePrintCompletableFuture;
    }

    @Override
    public CompletableFuture<Long> executePersistVariable(Script script) {
        final Long scriptId = scriptStateManager.generateScriptId();
        CompletableFuture<Long> executePersistVariableCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ScriptState instantScriptState = ScriptState.of(scriptId, script.userId(), script.programmingLanguage(), script.scriptBody());
            scriptStateManager.saveIntermediateScriptState(scriptId, instantScriptState);

            final String scriptBody = script.scriptBody();

            verifyIsJavascript(script.programmingLanguage());
            scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.RUNNING);
            ScriptState runningScriptState = ScriptState.updateStartAt(
                    scriptStateManager.findScriptState(scriptId).orElseThrow()
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, runningScriptState);

            var contextConfiguration = PolyglotContextConfigurationHolder.create(scriptId, true);
            Context context = getContext(contextConfiguration);

            context.eval(JS_LANGUAGE_ID, scriptBody);

            Value bindings = context.getBindings(JS_LANGUAGE_ID);

            final String prefix = "EXECUTOR__";
            final StringBuilder sb = new StringBuilder();
            bindings.getMemberKeys().forEach(memberKey -> {
                if (memberKey.startsWith(prefix)) {
                    String trimmedMemberKey = memberKey.replace(prefix, "");
                    Value memberValue = bindings.getMember(memberKey);
                    String retrievedMemberValue = PolyglotValueUtils.retrieveAsString(memberValue);
                    sb.append("PersistedVariable[").append(trimmedMemberKey).append("=").append(retrievedMemberValue).append("]\n");
                }
            });

            ScriptState persistedMembersScriptState = ScriptState.updateResult(
                    scriptStateManager.findScriptState(scriptId).orElseThrow(),
                    sb.toString()
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, persistedMembersScriptState);

            scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.COMPLETED);
            ScriptState finishedScriptState = scriptStateManager.findScriptState(scriptId).orElseThrow();
            ScriptState completedScriptState = ScriptState.updateResult(
                    finishedScriptState,
                    finishedScriptState.consoleResult() + "Persist variable execution is finished.\n"
            );
            scriptStateManager.saveIntermediateScriptState(scriptId, completedScriptState);

            return scriptId;
        }, executorService).whenComplete((res, ex) -> {
            if (Objects.nonNull(ex)) {
                scriptStateManager.changeScriptExecutionStatus(scriptId, ExecutionStatus.CANCELED);
                ScriptState scriptState = ScriptState.updateError(
                        scriptStateManager.findScriptState(scriptId).orElseThrow(),
                        ex.getMessage()
                );
                scriptStateManager.saveIntermediateScriptState(scriptId, scriptState);
                processException(ex);
            }

            cleanUpRunningJob(scriptId);
            scriptStateManager.persistScriptStateCompletion(scriptId);
        });

        runningJobs.put(scriptId, executePersistVariableCompletableFuture);
        return executePersistVariableCompletableFuture;
    }

    private void verifyIsJavascript(ProgrammingLanguage programmingLanguage) {
        if (!programmingLanguage.equals(ProgrammingLanguage.JAVASCRIPT)) {
            log.info("Thread-[{}] : Provided ProgrammingLanguage[{}] in JavaScriptExecutor.", Thread.currentThread().getName(), programmingLanguage);
            throw new UnsupportedProgrammingLanguageException("JavaScriptExecutor does not support provided programming language.");
        }
    }

    private void processException(Throwable exception) {
        if (exception instanceof UnsupportedProgrammingLanguageException unsupportedProgrammingLanguageException) {
            log.info("Thread-[{}] : The provided programming language does not support by current service.", Thread.currentThread().getName());
            log.info(LOG_EXECUTION_CANCELLED_MESSAGE, Thread.currentThread().getName(), unsupportedProgrammingLanguageException.getMessage());
        } else if (exception instanceof PolyglotException polyglotException) {
            log.info("Thread-[{}] : The guest language code parsing or evaluation failed.", Thread.currentThread().getName());
            log.info(LOG_EXECUTION_CANCELLED_MESSAGE, Thread.currentThread().getName(), polyglotException.getMessage());
        } else if (exception instanceof IllegalArgumentException illegalArgumentException) {
            log.error("Thread-[{}] : The language does not exist or is not accessible.", Thread.currentThread());
            log.error(LOG_EXECUTION_CANCELLED_MESSAGE, Thread.currentThread(), illegalArgumentException.getMessage());
        } else if (exception instanceof IllegalStateException illegalStateException) {
            log.error("Thread-[{}] : The context is already closed and the current thread is not allowed to access it.", Thread.currentThread());
            log.error(LOG_EXECUTION_CANCELLED_MESSAGE, Thread.currentThread(), illegalStateException.getMessage());
        } else {
            log.warn("Thread-[{}] : Script execution cancelled with unexpected exception.", Thread.currentThread().getName());
            log.warn(LOG_EXECUTION_CANCELLED_MESSAGE, Thread.currentThread().getName(), exception.getMessage());
        }
    }

    private void cleanUpRunningJob(Long scriptId) {
        runningJobs.remove(scriptId);
    }
}
