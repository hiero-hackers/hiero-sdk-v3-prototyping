package org.hiero.sdk.v3.codegen.parser;

import java.util.Objects;
import org.hiero.sdk.v3.codegen.model.SourceLocation;

public record ExtractedSchema(String sourcePath, String content, int schemaStartLine) {

    public ExtractedSchema {
        sourcePath = SourceLocation.requireRepositoryRelative(sourcePath);
        content = Objects.requireNonNull(content, "content").replace("\r\n", "\n").replace('\r', '\n');
        if (schemaStartLine < 1) {
            throw new IllegalArgumentException("schemaStartLine must be positive");
        }
    }

    public SourceLocation location() {
        return new SourceLocation(sourcePath, schemaStartLine, 1);
    }
}
