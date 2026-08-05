package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record EnumDeclaration(
        String name,
        List<TypeReference> parents,
        List<AnnotationUse> annotations,
        List<EnumValue> values,
        List<FieldDeclaration> fields,
        List<MethodDeclaration> methods,
        String documentation,
        SourceLocation location)
        implements Declaration {

    public EnumDeclaration {
        if (Objects.requireNonNull(name, "name").isBlank()) {
            throw new IllegalArgumentException("enum name must not be blank");
        }
        parents = List.copyOf(parents);
        annotations = List.copyOf(annotations);
        values = List.copyOf(values);
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
        documentation = Objects.requireNonNull(documentation, "documentation");
        Objects.requireNonNull(location, "location");
    }
}
