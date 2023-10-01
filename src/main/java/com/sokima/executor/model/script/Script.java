package com.sokima.executor.model.script;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record Script(
        @NotNull @NotBlank String userId,
        @NotNull ProgrammingLanguage programmingLanguage,
        @NotNull ExecutionMethod executionMethod,
        @NotNull @NotBlank String scriptBody,
        @Nullable Object[] args
) implements Serializable {
}
