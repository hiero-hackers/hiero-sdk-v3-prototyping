package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record SchemaDocument(
        String sourcePath,
        String namespace,
        int schemaStartLine,
        List<ImportDeclaration> imports,
        List<Declaration> declarations) {

    public SchemaDocument {
        sourcePath = SourceLocation.requireRepositoryRelative(sourcePath);
        if (Objects.requireNonNull(namespace, "namespace").isBlank()) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        if (schemaStartLine < 1) {
            throw new IllegalArgumentException("schemaStartLine must be positive");
        }
        imports = List.copyOf(imports);
        declarations = List.copyOf(declarations);
    }
}
