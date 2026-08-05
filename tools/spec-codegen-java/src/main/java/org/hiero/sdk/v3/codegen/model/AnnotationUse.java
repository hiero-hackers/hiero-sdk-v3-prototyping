package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record AnnotationUse(String name, List<String> arguments, SourceLocation location) {

    public AnnotationUse {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("annotation name must not be blank");
        }
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(location, "location");
    }
}
