package org.hiero.sdk.v3.codegen.model;

import java.util.Objects;

public record WildcardTypeReference(SourceLocation location) implements TypeReference {

    public WildcardTypeReference {
        Objects.requireNonNull(location, "location");
    }
}
