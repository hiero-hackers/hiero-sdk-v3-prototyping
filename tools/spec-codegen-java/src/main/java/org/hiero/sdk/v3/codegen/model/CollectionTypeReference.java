package org.hiero.sdk.v3.codegen.model;

import java.util.Objects;

public record CollectionTypeReference(CollectionKind kind, TypeReference elementType, SourceLocation location)
        implements TypeReference {

    public CollectionTypeReference {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(elementType, "elementType");
        Objects.requireNonNull(location, "location");
    }
}
