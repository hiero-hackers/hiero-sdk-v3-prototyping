package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record TypeDeclaration(
        String name,
        TypeKind kind,
        List<GenericParameter> typeParameters,
        List<TypeReference> parents,
        List<AnnotationUse> annotations,
        List<FieldDeclaration> fields,
        List<MethodDeclaration> methods,
        String documentation,
        SourceLocation location)
        implements Declaration {

    public TypeDeclaration {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("type name must not be blank");
        }
        Objects.requireNonNull(kind, "kind");
        typeParameters = List.copyOf(typeParameters);
        parents = List.copyOf(parents);
        annotations = List.copyOf(annotations);
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
        documentation = Objects.requireNonNull(documentation, "documentation");
        Objects.requireNonNull(location, "location");
    }
}
