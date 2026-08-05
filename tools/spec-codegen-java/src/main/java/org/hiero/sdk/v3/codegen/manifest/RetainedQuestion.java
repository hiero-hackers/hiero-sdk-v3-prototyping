package org.hiero.sdk.v3.codegen.manifest;

import java.util.Objects;
import org.hiero.sdk.v3.codegen.model.SourceLocation;

public record RetainedQuestion(String sourcePath, String text) {
    public RetainedQuestion {
        sourcePath = SourceLocation.requireRepositoryRelative(sourcePath);
        if (Objects.requireNonNull(text, "text").isBlank()) {
            throw new IllegalArgumentException("question text must not be blank");
        }
    }
}
