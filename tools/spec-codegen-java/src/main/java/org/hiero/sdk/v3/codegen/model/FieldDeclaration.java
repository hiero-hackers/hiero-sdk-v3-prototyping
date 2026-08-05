package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record FieldDeclaration(
        String name,
        TypeReference type,
        List<AnnotationUse> annotations,
        String documentation,
        SourceLocation location) {

    public FieldDeclaration {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("field name must not be blank");
        }
        Objects.requireNonNull(type, "type");
        annotations = List.copyOf(annotations);
        documentation = Objects.requireNonNull(documentation, "documentation");
        Objects.requireNonNull(location, "location");
    }
}
