package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record MethodDeclaration(
        String name,
        TypeReference returnType,
        List<Parameter> parameters,
        List<AnnotationUse> annotations,
        String documentation,
        SourceLocation location) {

    public MethodDeclaration {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("method name must not be blank");
        }
        Objects.requireNonNull(returnType, "returnType");
        parameters = List.copyOf(parameters);
        annotations = List.copyOf(annotations);
        documentation = Objects.requireNonNull(documentation, "documentation");
        Objects.requireNonNull(location, "location");
    }
}
