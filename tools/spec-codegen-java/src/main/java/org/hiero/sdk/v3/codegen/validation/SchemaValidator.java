package org.hiero.sdk.v3.codegen.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.hiero.sdk.v3.codegen.model.AnnotationUse;
import org.hiero.sdk.v3.codegen.model.ConstantDeclaration;
import org.hiero.sdk.v3.codegen.model.Declaration;
import org.hiero.sdk.v3.codegen.model.EnumDeclaration;
import org.hiero.sdk.v3.codegen.model.MappingDecision;
import org.hiero.sdk.v3.codegen.model.NamespaceOperationDeclaration;
import org.hiero.sdk.v3.codegen.model.ResolvedSchema;
import org.hiero.sdk.v3.codegen.model.SchemaDocument;
import org.hiero.sdk.v3.codegen.model.SchemaElements;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.hiero.sdk.v3.codegen.model.SourceLocation;
import org.hiero.sdk.v3.codegen.model.TypeDeclaration;

public final class SchemaValidator {

    private static final Set<String> SUPPORTED_ANNOTATIONS = Set.of(
            "async", "default", "finalType", "immutable", "maxLength", "min", "minLength",
            "nullable", "oneOf", "override", "sealed", "static", "throws");

    private final SchemaResolver resolver = new SchemaResolver();

    public ResolvedSchema validate(List<SchemaDocument> documents) {
        for (var document : documents) {
            document.declarations().forEach(this::validateAnnotations);
        }
        return resolver.resolve(documents);
    }

    public void validateMappings(ResolvedSchema schema, List<MappingDecision> mappings) {
        var counts = new HashMap<String, Integer>();
        mappings.forEach(mapping -> counts.merge(mapping.sourceElement(), 1, Integer::sum));
        var fallback = schema.documents().stream().findFirst()
                .map(document -> new SourceLocation(document.sourcePath(), document.schemaStartLine(), 1))
                .orElse(new SourceLocation("spec/base", 1, 1));
        counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .sorted(java.util.Map.Entry.comparingByKey())
                .findFirst()
                .ifPresent(entry -> {
                    throw new SchemaException("MULTIPLY_MAPPED_ELEMENT", fallback,
                            "source element '" + entry.getKey() + "' has " + entry.getValue() + " mappings");
                });
        SchemaElements.identities(schema).stream()
                .sorted()
                .filter(identity -> counts.getOrDefault(identity, 0) == 0)
                .findFirst()
                .ifPresent(identity -> {
                    throw new SchemaException("UNMAPPED_ELEMENT", fallback,
                            "source element '" + identity + "' has no mapping");
                });
    }

    private void validateAnnotations(Declaration declaration) {
        check(declaration.annotations());
        switch (declaration) {
            case TypeDeclaration type -> {
                type.fields().forEach(field -> check(field.annotations()));
                type.methods().forEach(method -> check(method.annotations()));
            }
            case EnumDeclaration enumeration -> {
                enumeration.fields().forEach(field -> check(field.annotations()));
                enumeration.methods().forEach(method -> check(method.annotations()));
            }
            case NamespaceOperationDeclaration ignored -> {
                // Declaration annotations delegate to the operation method.
            }
            case ConstantDeclaration ignored -> {
                // Constants have only declaration annotations.
            }
        }
    }

    private void check(List<AnnotationUse> annotations) {
        annotations.stream()
                .filter(annotation -> !SUPPORTED_ANNOTATIONS.contains(annotation.name()))
                .findFirst()
                .ifPresent(annotation -> {
                    throw new SchemaException("UNSUPPORTED_ANNOTATION", annotation.location(),
                            "annotation '@@" + annotation.name() + "' is not supported");
                });
    }
}
