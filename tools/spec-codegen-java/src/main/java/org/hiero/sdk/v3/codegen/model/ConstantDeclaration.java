package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record ConstantDeclaration(
        String name,
        TypeReference type,
        String value,
        List<AnnotationUse> annotations,
        String documentation,
        SourceLocation location)
        implements Declaration {

    public ConstantDeclaration {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("constant name must not be blank");
        }
        Objects.requireNonNull(type, "type");
        value = Objects.requireNonNull(value, "value");
        annotations = List.copyOf(annotations);
        documentation = Objects.requireNonNull(documentation, "documentation");
        Objects.requireNonNull(location, "location");
    }
}
