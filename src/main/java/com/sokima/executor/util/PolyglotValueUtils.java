package com.sokima.executor.util;

import com.sokima.executor.util.exception.IllegalInstantiationException;
import org.graalvm.polyglot.Value;

import java.util.List;

public final class PolyglotValueUtils {

    private PolyglotValueUtils() {
        throw new IllegalInstantiationException("Forbidden instantiation of util class.");
    }

    public static String retrieveAsString(Value value) {
        if (value.isString()) {
            return value.asString();
        }

        if (value.isNull()) {
            return "null";
        }

        if (value.isBoolean()) {
            return Boolean.toString(value.asBoolean());
        }

        if (value.isDate()) {
            return value.asDate().toString();
        }

        if (value.isDuration()) {
            return value.asDuration().toString();
        }

        if (value.isInstant()) {
            return value.asInstant().toString();
        }

        if (value.isTime()) {
            return value.asTime().toString();
        }

        if (value.isTimeZone()) {
            return value.asTimeZone().toString();
        }

        if (value.isNumber()) {
            if (value.fitsInByte()) {
                return Byte.toString(value.asByte());
            }

            if (value.fitsInShort()) {
                return Short.toString(value.asShort());
            }

            if (value.fitsInInt()) {
                return Integer.toString(value.asInt());
            }

            if (value.fitsInLong()) {
                return Long.toString(value.asLong());
            }

            if (value.fitsInFloat()) {
                return Float.toString(value.asFloat());
            }

            if (value.fitsInDouble()) {
                return Double.toString(value.asDouble());
            }
        }

        if (value.isIterator()) {
            return value.getIterator().as(List.class).toString();
        }

        return "undefined";
    }
}
