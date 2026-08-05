package org.hiero.sdk.v3.codegen.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hiero.sdk.v3.codegen.model.CollectionTypeReference;
import org.hiero.sdk.v3.codegen.model.ConstantDeclaration;
import org.hiero.sdk.v3.codegen.model.Declaration;
import org.hiero.sdk.v3.codegen.model.EnumDeclaration;
import org.hiero.sdk.v3.codegen.model.NamedTypeReference;
import org.hiero.sdk.v3.codegen.model.NamespaceOperationDeclaration;
import org.hiero.sdk.v3.codegen.model.ResolvedSchema;
import org.hiero.sdk.v3.codegen.model.SchemaDocument;
import org.hiero.sdk.v3.codegen.model.SchemaException;
import org.hiero.sdk.v3.codegen.model.TypeDeclaration;
import org.hiero.sdk.v3.codegen.model.TypeReference;
import org.hiero.sdk.v3.codegen.model.TypeVariableReference;
import org.hiero.sdk.v3.codegen.model.SchemaElements;

public final class SchemaResolver {

    public ResolvedSchema resolve(List<SchemaDocument> documents) {
        var namespaces = new LinkedHashMap<String, SchemaDocument>();
        var declarations = new LinkedHashMap<String, Declaration>();
        for (var document : documents) {
            if (namespaces.putIfAbsent(document.namespace(), document) != null) {
                throw error("DUPLICATE_NAMESPACE", document.declarations().isEmpty() ? null : document.declarations().getFirst(),
                        document, "duplicate namespace '" + document.namespace() + "'");
            }
            var localNames = new HashSet<String>();
            for (var declaration : document.declarations()) {
                if (declaration instanceof NamespaceOperationDeclaration) {
                    continue;
                }
                if (!localNames.add(declaration.name())) {
                    throw new SchemaException("DUPLICATE_DECLARATION", declaration.location(),
                            "duplicate declaration '" + declaration.name() + "'");
                }
                declarations.put(document.namespace() + "." + declaration.name(), declaration);
            }
            validateDuplicateMembers(document);
        }

        for (var document : documents) {
            validateDocument(document, namespaces, declarations);
        }
        return new ResolvedSchema(documents, declarations);
    }

    private void validateDuplicateMembers(SchemaDocument document) {
        var namespaceOperations = new HashSet<String>();
        for (var declaration : document.declarations()) {
            switch (declaration) {
                case TypeDeclaration type -> {
                    validateUniqueNames(type.fields().stream().map(field -> field.name()).toList(), type.location());
                    validateUniqueNames(type.methods().stream().map(SchemaResolver::methodSignature).toList(), type.location());
                }
                case EnumDeclaration enumeration -> {
                    validateUniqueNames(enumeration.values().stream().map(value -> value.name()).toList(), enumeration.location());
                    validateUniqueNames(enumeration.fields().stream().map(field -> field.name()).toList(), enumeration.location());
                    validateUniqueNames(enumeration.methods().stream().map(SchemaResolver::methodSignature).toList(), enumeration.location());
                }
                case NamespaceOperationDeclaration operation -> {
                    var signature = methodSignature(operation.method()) + ":"
                            + SchemaElements.display(operation.method().returnType());
                    if (!namespaceOperations.add(signature)) {
                        throw new SchemaException("DUPLICATE_DECLARATION", operation.location(),
                                "duplicate namespace operation '" + signature + "'");
                    }
                }
                case ConstantDeclaration ignored -> {
                }
            }
        }
    }

    private static void validateUniqueNames(List<String> names, org.hiero.sdk.v3.codegen.model.SourceLocation location) {
        var unique = new HashSet<String>();
        names.stream().filter(name -> !unique.add(name)).findFirst().ifPresent(name -> {
            throw new SchemaException("DUPLICATE_DECLARATION", location, "duplicate member '" + name + "'");
        });
    }

    private static String methodSignature(org.hiero.sdk.v3.codegen.model.MethodDeclaration method) {
        return method.name() + "(" + method.parameters().stream()
                .map(parameter -> SchemaElements.display(parameter.type()) + (parameter.varargs() ? "..." : ""))
                .collect(java.util.stream.Collectors.joining(",")) + ")";
    }

    private void validateDocument(
            SchemaDocument document,
            Map<String, SchemaDocument> namespaces,
            Map<String, Declaration> declarations) {
        var imports = new HashMap<String, String>();
        for (var imported : document.imports()) {
            if (!namespaces.containsKey(imported.namespace())) {
                throw new SchemaException("UNRESOLVED_NAMESPACE", imported.location(),
                        "namespace '" + imported.namespace() + "' does not exist");
            }
            for (var typeName : imported.typeNames()) {
                if (!declarations.containsKey(imported.namespace() + "." + typeName)) {
                    throw new SchemaException("UNRESOLVED_TYPE", imported.location(),
                            "type '" + typeName + "' does not exist in namespace '" + imported.namespace() + "'");
                }
                var previous = imports.putIfAbsent(typeName, imported.namespace());
                if (previous != null && !previous.equals(imported.namespace())) {
                    throw new SchemaException("AMBIGUOUS_IMPORT", imported.location(),
                            "type '" + typeName + "' is imported from multiple namespaces");
                }
            }
        }

        var usedImports = new HashSet<String>();
        for (var declaration : document.declarations()) {
            validateDeclaration(declaration, document, declarations, imports, usedImports);
        }
        for (var imported : document.imports()) {
            for (var typeName : imported.typeNames()) {
                if (!usedImports.contains(typeName)) {
                    throw new SchemaException("UNUSED_IMPORT", imported.location(),
                            "imported type '" + typeName + "' is not used");
                }
            }
        }
    }

    private void validateDeclaration(
            Declaration declaration,
            SchemaDocument document,
            Map<String, Declaration> declarations,
            Map<String, String> imports,
            Set<String> usedImports) {
        switch (declaration) {
            case TypeDeclaration type -> {
                var variables = new HashSet<String>();
                type.typeParameters().forEach(parameter -> variables.add(parameter.name()));
                type.typeParameters().forEach(parameter -> validateType(
                        parameter.bound(), variables, document, declarations, imports, usedImports));
                type.parents().forEach(parent -> validateType(parent, variables, document, declarations, imports, usedImports));
                type.fields().forEach(field -> validateType(field.type(), variables, document, declarations, imports, usedImports));
                type.methods().forEach(method -> validateMethod(method.returnType(), method.parameters().stream()
                        .map(parameter -> parameter.type()).toList(), variables, document, declarations, imports, usedImports));
            }
            case EnumDeclaration enumeration -> {
                enumeration.parents().forEach(parent -> validateType(parent, Set.of(), document, declarations, imports, usedImports));
                enumeration.fields().forEach(field -> validateType(field.type(), Set.of(), document, declarations, imports, usedImports));
                enumeration.methods().forEach(method -> validateMethod(method.returnType(), method.parameters().stream()
                        .map(parameter -> parameter.type()).toList(), Set.of(), document, declarations, imports, usedImports));
            }
            case ConstantDeclaration constant ->
                    validateType(constant.type(), Set.of(), document, declarations, imports, usedImports);
            case NamespaceOperationDeclaration operation -> validateMethod(
                    operation.method().returnType(),
                    operation.method().parameters().stream().map(parameter -> parameter.type()).toList(),
                    Set.of(), document, declarations, imports, usedImports);
        }
    }

    private void validateMethod(
            TypeReference returnType,
            List<TypeReference> parameters,
            Set<String> variables,
            SchemaDocument document,
            Map<String, Declaration> declarations,
            Map<String, String> imports,
            Set<String> usedImports) {
        validateType(returnType, variables, document, declarations, imports, usedImports);
        parameters.forEach(type -> validateType(type, variables, document, declarations, imports, usedImports));
    }

    private void validateType(
            TypeReference type,
            Set<String> variables,
            SchemaDocument document,
            Map<String, Declaration> declarations,
            Map<String, String> imports,
            Set<String> usedImports) {
        if (type == null) {
            return;
        }
        switch (type) {
            case TypeVariableReference variable -> {
                if (!variables.contains(variable.name())) {
                    throw new SchemaException("UNRESOLVED_TYPE_VARIABLE", variable.location(),
                            "type variable '$$" + variable.name() + "' is not declared");
                }
            }
            case NamedTypeReference named -> {
                resolveNamed(named, document, declarations, imports, usedImports);
                named.arguments().forEach(argument -> validateType(
                        argument, variables, document, declarations, imports, usedImports));
            }
            case CollectionTypeReference collection -> validateType(
                    collection.elementType(), variables, document, declarations, imports, usedImports);
            default -> {
                // Primitive and wildcard references are intrinsically resolved.
            }
        }
    }

    private void resolveNamed(
            NamedTypeReference named,
            SchemaDocument document,
            Map<String, Declaration> declarations,
            Map<String, String> imports,
            Set<String> usedImports) {
        var name = named.name();
        if (name.contains(".")) {
            if (!declarations.containsKey(name)) {
                throw new SchemaException("UNRESOLVED_TYPE", named.location(), "type '" + name + "' does not exist");
            }
            return;
        }
        if (declarations.containsKey(document.namespace() + "." + name)) {
            return;
        }
        if (imports.containsKey(name)) {
            usedImports.add(name);
            return;
        }
        throw new SchemaException("UNRESOLVED_TYPE", named.location(), "type '" + name + "' is not in scope");
    }

    private SchemaException error(
            String code, Declaration declaration, SchemaDocument document, String message) {
        var location = declaration == null
                ? new org.hiero.sdk.v3.codegen.model.SourceLocation(document.sourcePath(), document.schemaStartLine(), 1)
                : declaration.location();
        return new SchemaException(code, location, message);
    }
}
