package com.sokima.executor.service;

import com.sokima.executor.model.script.ScriptState;

import java.util.List;
import java.util.Optional;

public interface ScriptSearchService {

    /**
     * Return a full info of specified script.
     *
     * @param scriptId the script identifier.
     * @return {@link ScriptState}
     */
    ScriptState findDetailedScriptState(Long scriptId);

    /**
     * Return only information about execution status of script,
     * if script ended execution returns result as well.
     *
     * @param scriptId the script identifier.
     * @return {@link ScriptState}
     */
    ScriptState findScriptExecutionResult(Long scriptId);

    /**
     * Return brief info about all launched user' scripts.
     * Filtering, sorting, pagination may be applied to the result.
     *
     * @param userId    the user identifier.
     * @param filterBy  the filter params string.
     * @param sortBy    the sort params string.
     * @param pageCount the number of page.
     * @return {@link List<ScriptState>}
     */
    List<ScriptState> findBriefScriptsState(String userId, Optional<String> filterBy, Optional<String> sortBy, int pageCount);
}
