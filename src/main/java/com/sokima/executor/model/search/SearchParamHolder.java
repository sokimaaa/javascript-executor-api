package com.sokima.executor.model.search;

import com.sokima.executor.model.script.ScriptState;
import com.sokima.executor.util.CastUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public record SearchParamHolder(
        Set<FilterParam> filterParams,
        Map<String, SortingOrderEnum> sortParams,
        int pageCount,
        int pageSize
) {
    public static SearchParamHolder of(
            Set<FilterParam> filterParams,
            Map<String, SortingOrderEnum> sortingParams,
            int pageCount,
            int pageSize
    ) {
        return new SearchParamHolder(filterParams, sortingParams, pageCount, pageSize);
    }

    public int skip() {
        return pageCount * pageSize;
    }

    public Comparator<ScriptState> comparator() {
        return (s1, s2) -> {
            int comparison = 0;
            for (Map.Entry<String, SortingOrderEnum> entry : sortParams.entrySet()) {
                Class<? extends ScriptState> clazz = s1.getClass();
                final String sortByFieldName = entry.getKey();

                try {
                    Field sortByField = clazz.getDeclaredField(sortByFieldName);
                    sortByField.setAccessible(true);

                    Object s1Value = sortByField.get(s1);
                    Object s2Value = sortByField.get(s2);

                    final SortingOrderEnum sortOrder = entry.getValue();
                    comparison += sortOrder.apply(s1Value, s2Value);
                } catch (NoSuchFieldException ex) {
                    log.info("Provided field name does not exist : " + entry);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }

                if (comparison != 0) {
                    return comparison;
                }
            }

            return comparison;
        };
    }

    public Predicate<ScriptState> predicate() {
        return scriptState -> {
            boolean test = true;

            Class<? extends ScriptState> clazz = scriptState.getClass();
            for (FilterParam filterParam : filterParams) {
                FilterOperatorEnum operator = filterParam.operator();
                final String filterByFieldName = filterParam.filter();

                try {
                    Field field = clazz.getDeclaredField(filterByFieldName);
                    field.setAccessible(true);

                    Class<?> type = field.getType();

                    Object actualValue = CastUtils.cast(type, field.get(scriptState));
                    Object comparedValue = CastUtils.castStringValue(type, filterParam.value());

                    test &= operator.evaluate(actualValue, comparedValue);
                } catch (NoSuchFieldException ex) {
                    log.info("Provided wrong filterBy : {}", filterParam);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }

            return test;
        };
    }
}
