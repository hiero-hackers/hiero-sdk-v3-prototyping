package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Objects;

public record NamespaceOperationDeclaration(MethodDeclaration method) implements Declaration {

    public NamespaceOperationDeclaration {
        Objects.requireNonNull(method, "method");
    }

    @Override
    public String name() {
        return method.name();
    }

    @Override
    public List<AnnotationUse> annotations() {
        return method.annotations();
    }

    @Override
    public String documentation() {
        return method.documentation();
    }

    @Override
    public SourceLocation location() {
        return method.location();
    }
}
