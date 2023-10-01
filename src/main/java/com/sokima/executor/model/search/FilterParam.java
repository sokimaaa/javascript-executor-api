package com.sokima.executor.model.search;

public record FilterParam(
        String filter,
        String value,
        FilterOperatorEnum operator
) {
    public static FilterParam of(String filter, String value, String operator) {
        return new FilterParam(
                filter,
                value,
                FilterOperatorEnum.of(operator)
        );
    }
}
