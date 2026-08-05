package org.hiero.sdk.v3.codegen.model;

import java.util.Objects;

public record PrimitiveTypeReference(PrimitiveKind kind, SourceLocation location) implements TypeReference {

    public PrimitiveTypeReference {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(location, "location");
    }
}
