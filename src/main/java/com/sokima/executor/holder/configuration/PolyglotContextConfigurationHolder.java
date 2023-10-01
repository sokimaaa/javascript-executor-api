package com.sokima.executor.holder.configuration;

public record PolyglotContextConfigurationHolder(
        String languageId,
        Long scriptId,
        boolean isPersistableConsole
) {
    public static PolyglotContextConfigurationHolder create(String languageId, Long scriptId, boolean isPersistableConsole) {
        return new PolyglotContextConfigurationHolder(languageId, scriptId, isPersistableConsole);
    }

    public static PolyglotContextConfigurationHolder create(Long scriptId, boolean isPersistableConsole) {
        return create("js", scriptId, isPersistableConsole);
    }

    public static PolyglotContextConfigurationHolder create(Long scriptId) {
        return create(scriptId, false);
    }
}
