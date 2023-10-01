package com.sokima.executor.util;

import com.sokima.executor.util.exception.IllegalInstantiationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class CompareUtils {

    private CompareUtils() {
        throw new IllegalInstantiationException("Forbidden instantiation of util class.");
    }

    /**
     * Compares two object and return integer value as result.
     * Both objects should be {@link Comparable<?>},
     * Otherwise exception will be thrown.
     *
     * @param o1 the comparable obj.
     * @param o2 the comparable obj.
     * @return A negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified object.
     */
    public static int compare(Object o1, Object o2) {
        Class<?> clazz = o1.getClass();

        try {
            Method compareTo = clazz.getMethod("compareTo", clazz);
            Object invoke = compareTo.invoke(o1, o2);
            return (int) Math.signum((int) invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}
