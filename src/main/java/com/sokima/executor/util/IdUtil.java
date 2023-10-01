package com.sokima.executor.util;

import com.sokima.executor.util.exception.IllegalInstantiationException;

import java.util.Random;

/**
 * Util for different algorithm of generation id.
 */
public final class IdUtil {

    private IdUtil() {
        throw new IllegalInstantiationException("Forbidden instantiation of util class.");
    }

    private static final long UPPER_BOUND = 100_000L;
    private static final long LOWER_BOUND = 0L;

    private static final Random SIMPLE_ID_GENERATOR = new Random();

    public static long generateSimpleId() {
        return SIMPLE_ID_GENERATOR.nextLong(LOWER_BOUND, UPPER_BOUND);
    }
}
