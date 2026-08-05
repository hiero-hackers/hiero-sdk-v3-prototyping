package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record NamedTypeReference(String name, List<TypeReference> arguments, SourceLocation location)
        implements TypeReference {

    public NamedTypeReference {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("type name must not be blank");
        }
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(location, "location");
    }
}
