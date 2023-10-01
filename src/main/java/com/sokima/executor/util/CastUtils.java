package com.sokima.executor.util;

import com.sokima.executor.util.exception.IllegalInstantiationException;

public final class CastUtils {

    private CastUtils() {
        throw new IllegalInstantiationException("Forbidden instantiation of util class.");
    }

    public static Object cast(Class<?> clazzToCast, Object objToCast) {
        return clazzToCast.cast(objToCast);
    }

    public static Object castStringValue(Class<?> clazzToCast, Object objToCast) {
        if (clazzToCast.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) clazzToCast, objToCast.toString());
        }

        if (clazzToCast == Long.class) {
            return Long.valueOf((String) objToCast);
        }

        return cast(clazzToCast, objToCast);
    }
}
