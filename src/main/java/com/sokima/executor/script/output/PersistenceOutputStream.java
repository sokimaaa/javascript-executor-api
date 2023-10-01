package com.sokima.executor.script.output;

import java.io.OutputStream;
import java.util.Objects;

public abstract class PersistenceOutputStream extends OutputStream {

    private static final String JS_PERSIST_CONSOLE_PATTERN = "^(\\w+)(=)(\\w+)$";

    private final StringBuilder stringBuilder;

    protected PersistenceOutputStream() {
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public void write(int b) {
        if (b == '\n') {
            persist();
            cleanUpStringBuilder();
        } else {
            stringBuilder.append((char) b);
        }
    }

    protected abstract void persist();

    protected Pair parseLine() {
        final String line = stringBuilder.toString();
        if (!line.matches(JS_PERSIST_CONSOLE_PATTERN)) {
            return Pair.empty();
        }

        String[] split = line.split("=");
        String key = split[0];
        String value = split[1];
        return key.isBlank() && value.isBlank() ? Pair.empty() : Pair.of(key, value);
    }

    private void cleanUpStringBuilder() {
        stringBuilder.delete(0, stringBuilder.length());
    }

    protected record Pair(
            String key,
            String value
    ) {
        public static Pair of(String key, String value) {
            return new Pair(key, value);
        }

        public static Pair empty() {
            return new Pair(null, null);
        }

        public boolean isEmpty() {
            return Objects.isNull(key) || Objects.isNull(value);
        }

        @Override
        public String toString() {
            return "ConsoleResult[" + key + "=" + value + "]\n";
        }
    }
}
