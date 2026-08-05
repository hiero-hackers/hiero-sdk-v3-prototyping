package org.hiero.sdk.v3.codegen.model;

import java.util.Objects;

public record EnumValue(String name, String documentation, SourceLocation location) {

    public EnumValue {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("enum value name must not be blank");
        }
        documentation = Objects.requireNonNull(documentation, "documentation");
        Objects.requireNonNull(location, "location");
    }
}
