package com.sokima.executor.util;

import com.sokima.executor.model.search.FilterParam;
import com.sokima.executor.model.search.SortingOrderEnum;
import com.sokima.executor.util.exception.IllegalInstantiationException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public final class QueryParamUtils {

    private QueryParamUtils() {
        throw new IllegalInstantiationException("Forbidden instantiation of util class.");
    }

    private static final String FILTERING_QUERY_PARAM_PATTERN = "^(\\w+=)(eq|gte|lte)(:\\w+)$";
    private static final int FILTER_KEY_INDEX = 0;
    private static final int FILTER_OPERATOR_INDEX = 1;
    private static final int FILTER_VALUE_INDEX = 2;

    public static Set<FilterParam> parsePathFilterParam(String pathFilterParam) {
        if (Objects.isNull(pathFilterParam) || pathFilterParam.isBlank()) {
            return Collections.emptySet();
        }

        String[] splitPathFilterParams = pathFilterParam.split(";");
        return parsePathFilterParam(splitPathFilterParams);
    }

    public static Set<FilterParam> parsePathFilterParam(String[] pathFilterParams) {
        if (pathFilterParams == null || pathFilterParams.length == 0) {
            return Collections.emptySet();
        }

        Set<FilterParam> parsedPathFilterParams = new HashSet<>();
        for (String pathFilterParam : pathFilterParams) {
            if (!pathFilterParam.toLowerCase().matches(FILTERING_QUERY_PARAM_PATTERN)) {
                log.info("FilterParam[{}] was ignored.", pathFilterParam);
                continue;
            }

            String[] split = pathFilterParam.split("(=)|(:)");
            final String filter = split[FILTER_KEY_INDEX];
            final String operator = split[FILTER_OPERATOR_INDEX];
            final String value = split[FILTER_VALUE_INDEX];

            FilterParam filterParam = FilterParam.of(filter, value, operator);
            parsedPathFilterParams.add(filterParam);
        }

        return parsedPathFilterParams;
    }

    private static final String SORTING_QUERY_PARAM_PATTERN = "^(asc|desc)\\(\\w+\\)$";
    private static final int SORT_ORDER_INDEX = 0;
    private static final int SORT_VALUE_INDEX = 1;

    public static Map<String, SortingOrderEnum> parseQuerySortParam(String querySortParam) {
        if (Objects.isNull(querySortParam) || querySortParam.isBlank()) {
            return Collections.emptyMap();
        }

        String[] splitQuerySortParams = querySortParam.split(",");
        return parseQuerySortParam(splitQuerySortParams);
    }

    public static Map<String, SortingOrderEnum> parseQuerySortParam(String[] querySortParams) {
        if (querySortParams == null || querySortParams.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, SortingOrderEnum> parsedQuerySortParams = new LinkedHashMap<>();
        for (String querySortParam : querySortParams) {
            if (!querySortParam.toLowerCase().matches(SORTING_QUERY_PARAM_PATTERN)) {
                log.info("QuerySortParam[{}] was ignored.", querySortParam);
                continue;
            }

            String[] split = querySortParam.split("(\\()|(\\))");
            final String order = split[SORT_ORDER_INDEX];
            final String value = split[SORT_VALUE_INDEX];
            parsedQuerySortParams.put(value, SortingOrderEnum.of(order));
        }

        return parsedQuerySortParams;
    }
}
