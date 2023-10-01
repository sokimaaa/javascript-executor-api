package com.sokima.executor.script.output;

import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.script.manager.ScriptStateManager;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class PersistenceOutputStreamImpl extends PersistenceOutputStream {

    private final Long scriptId;

    private final ScriptStateManager scriptStateManager;

    protected void persist() {
        Pair parsedConsoleLine = parseLine();
        if (!parsedConsoleLine.isEmpty()) {
            scriptStateManager.findScriptState(scriptId).ifPresent(scriptState -> {
                        final String consoleResult = scriptState.consoleResult();
                        final String newConsoleResult = Objects.isNull(consoleResult) ? parsedConsoleLine.toString() : consoleResult + parsedConsoleLine;
                        final ScriptState updatedScriptState = ScriptState.updateResult(scriptState, newConsoleResult);
                        scriptStateManager.saveIntermediateScriptState(scriptId, updatedScriptState);
                    }
            );
        }
    }
}
