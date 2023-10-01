package com.sokima.executor.model.search;

import com.sokima.executor.util.CompareUtils;

public enum FilterOperatorEnum {
    EQ, LTE, GTE;

    public static FilterOperatorEnum of(String value) {
        for (FilterOperatorEnum filterOperatorEnum : FilterOperatorEnum.values()) {
            if (value.equalsIgnoreCase(filterOperatorEnum.name())) {
                return filterOperatorEnum;
            }
        }

        throw new IllegalArgumentException("No enum constant : " + value);
    }

    public boolean evaluate(Object actualValue, Object comparedValue) {
        if (this == EQ) {
            return actualValue.equals(comparedValue);
        }

        if (this == GTE) {
            if (actualValue instanceof Comparable<?> && comparedValue instanceof Comparable<?>) {
                return CompareUtils.compare(actualValue, comparedValue) >= 0;
            }
        }

        if (this == LTE) {
            if (actualValue instanceof Comparable<?> && comparedValue instanceof Comparable<?>) {
                return CompareUtils.compare(actualValue, comparedValue) <= 0;
            }
        }

        return true;
    }
}
