package org.hiero.sdk.v3.codegen.model;

import java.util.List;
import java.util.Map;

public record ResolvedSchema(List<SchemaDocument> documents, Map<String, Declaration> declarationsByQualifiedName) {

    public ResolvedSchema {
        documents = List.copyOf(documents);
        declarationsByQualifiedName = Map.copyOf(declarationsByQualifiedName);
    }
}
