package org.hiero.sdk.v3.codegen.model;

public sealed interface TypeReference
        permits PrimitiveTypeReference,
                NamedTypeReference,
                TypeVariableReference,
                CollectionTypeReference,
                WildcardTypeReference {

    SourceLocation location();
}
