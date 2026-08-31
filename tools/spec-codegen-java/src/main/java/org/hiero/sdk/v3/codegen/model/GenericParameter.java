package org.hiero.sdk.v3.codegen.model;

import java.util.Objects;

public record GenericParameter(String name, TypeReference bound, SourceLocation location) {

    public GenericParameter {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("generic parameter name must not be blank");
        }
        Objects.requireNonNull(location, "location");
    }
}
