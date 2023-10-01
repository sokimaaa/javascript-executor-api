package com.sokima.executor.service.v1;

import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.model.search.FilterParam;
import com.sokima.executor.model.search.SearchParamHolder;
import com.sokima.executor.model.search.SortingOrderEnum;
import com.sokima.executor.script.manager.ScriptStateManager;
import com.sokima.executor.service.ScriptSearchService;
import com.sokima.executor.service.exception.SearchFailException;
import com.sokima.executor.util.QueryParamUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Qualifier("scriptSearchServiceV1")
@RequiredArgsConstructor
public class ScriptSearchServiceV1 implements ScriptSearchService {

    @Value("${executor.page.size}")
    private int pageSize;

    private final ScriptStateManager scriptStateManager;

    @Override
    public ScriptState findDetailedScriptState(Long scriptId) {
        return scriptStateManager.findScriptState(scriptId)
                .orElseThrow(() -> {
                    throw new SearchFailException("Not found Script with " + scriptId + " id.", scriptId.toString());
                });
    }

    @Override
    public ScriptState findScriptExecutionResult(Long scriptId) {
        return scriptStateManager.findExecutionResult(scriptId)
                .orElseThrow(() -> {
                    throw new SearchFailException("Not found Script with " + scriptId + " id.", scriptId.toString());
                });
    }

    @Override
    public List<ScriptState> findBriefScriptsState(String userId, Optional<String> filterBy, Optional<String> sortBy, int pageCount) {
        Set<FilterParam> filterParams = filterBy.map(QueryParamUtils::parsePathFilterParam)
                .orElse(Collections.emptySet());
        Map<String, SortingOrderEnum> sortingParams = sortBy.map(QueryParamUtils::parseQuerySortParam)
                .orElse(Collections.emptyMap());

        SearchParamHolder holder = SearchParamHolder.of(filterParams, sortingParams, pageCount, pageSize);
        return scriptStateManager.findScriptStatesByUser(userId, holder);
    }
}
