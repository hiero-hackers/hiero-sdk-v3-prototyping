package org.hiero.sdk.v3.codegen.model;

import java.util.Objects;

public record Parameter(String name, TypeReference type, boolean varargs, SourceLocation location) {

    public Parameter {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("parameter name must not be blank");
        }
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(location, "location");
    }
}
