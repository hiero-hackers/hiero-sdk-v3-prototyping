package org.hiero.sdk.v3.codegen.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SchemaElements {

    private SchemaElements() {}

    public static Set<String> identities(ResolvedSchema schema) {
        var identities = new LinkedHashSet<String>();
        schema.documents().stream()
                .sorted(java.util.Comparator.comparing(SchemaDocument::namespace))
                .forEach(document -> document.declarations().forEach(declaration -> addDeclaration(
                        identities, document.namespace(), declaration)));
        return Set.copyOf(identities);
    }

    private static void addDeclaration(Set<String> identities, String namespace, Declaration declaration) {
        if (declaration instanceof NamespaceOperationDeclaration operation) {
            addNamespaceMethod(identities, namespace, operation.method());
            return;
        }
        var base = namespace + "#" + declaration.name();
        identities.add(base);
        addAnnotations(identities, base, declaration.annotations());

        switch (declaration) {
            case TypeDeclaration type -> {
                type.fields().forEach(field -> addField(identities, base, field));
                type.methods().forEach(method -> addMethod(identities, base, method));
            }
            case EnumDeclaration enumeration -> {
                enumeration.values().forEach(value -> identities.add(base + "." + value.name()));
                enumeration.fields().forEach(field -> addField(identities, base, field));
                enumeration.methods().forEach(method -> addMethod(identities, base, method));
            }
            case ConstantDeclaration ignored -> {}
            case NamespaceOperationDeclaration ignored -> throw new IllegalStateException("handled above");
        }
    }

    private static void addField(Set<String> identities, String base, FieldDeclaration field) {
        var identity = base + "." + field.name();
        identities.add(identity);
        addAnnotations(identities, identity, field.annotations());
    }

    private static void addMethod(Set<String> identities, String base, MethodDeclaration method) {
        var parameterTypes = new ArrayList<String>();
        method.parameters().forEach(parameter -> parameterTypes.add(display(parameter.type())
                + (parameter.varargs() ? "..." : "")));
        var identity = base + "." + method.name() + "(" + String.join(",", parameterTypes) + ")";
        identities.add(identity);
        addAnnotations(identities, identity, method.annotations());
    }

    private static void addNamespaceMethod(Set<String> identities, String namespace, MethodDeclaration method) {
        var parameterTypes = new ArrayList<String>();
        method.parameters().forEach(parameter -> parameterTypes.add(display(parameter.type())
                + (parameter.varargs() ? "..." : "")));
        var identity = namespace + "#namespace." + method.name() + "(" + String.join(",", parameterTypes)
                + "):" + display(method.returnType());
        identities.add(identity);
        addAnnotations(identities, identity, method.annotations());
    }

    private static void addAnnotations(Set<String> identities, String base, List<AnnotationUse> annotations) {
        for (var index = 0; index < annotations.size(); index++) {
            identities.add(base + "@@" + annotations.get(index).name() + "[" + index + "]");
        }
    }

    public static String display(TypeReference type) {
        return switch (type) {
            case PrimitiveTypeReference primitive -> primitive.kind().schemaName();
            case NamedTypeReference named -> named.name()
                    + (named.arguments().isEmpty()
                            ? ""
                            : "<" + named.arguments().stream()
                                    .map(SchemaElements::display)
                                    .collect(java.util.stream.Collectors.joining(",")) + ">");
            case TypeVariableReference variable -> "$$" + variable.name();
            case CollectionTypeReference collection -> collection.kind().name().toLowerCase()
                    + "<" + display(collection.elementType()) + ">";
            case WildcardTypeReference ignored -> "ANY";
        };
    }
}
