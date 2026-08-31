package org.hiero.sdk.v3.codegen.model;

import java.util.Objects;

public record TypeVariableReference(String name, SourceLocation location) implements TypeReference {

    public TypeVariableReference {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("type variable name must not be blank");
        }
        Objects.requireNonNull(location, "location");
    }
}
