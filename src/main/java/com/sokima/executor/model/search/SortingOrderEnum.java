package com.sokima.executor.model.search;

import com.sokima.executor.util.CompareUtils;

public enum SortingOrderEnum {
    ASC, DESC;

    public static SortingOrderEnum of(String value) {
        for (SortingOrderEnum sortingOrderEnum : SortingOrderEnum.values()) {
            if (value.equalsIgnoreCase(sortingOrderEnum.name())) {
                return sortingOrderEnum;
            }
        }

        throw new IllegalArgumentException("No enum constant : " + value);
    }

    public int apply(Object o1, Object o2) {
        if (!(o1 instanceof Comparable<?> && o2 instanceof Comparable<?>)) {
            return 0;
        }

        final int comparison = CompareUtils.compare(o1, o2);
        return this == ASC ? comparison : -comparison;
    }
}
