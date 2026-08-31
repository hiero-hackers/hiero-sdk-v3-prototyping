package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record ImportDeclaration(List<String> typeNames, String namespace, SourceLocation location) {

    public ImportDeclaration {
        typeNames = List.copyOf(typeNames);
        if (typeNames.isEmpty()) {
            throw new IllegalArgumentException("import must contain at least one type");
        }
        if (Objects.requireNonNull(namespace, "namespace").isBlank()) {
            throw new IllegalArgumentException("import namespace must not be blank");
        }
        Objects.requireNonNull(location, "location");
    }
}
