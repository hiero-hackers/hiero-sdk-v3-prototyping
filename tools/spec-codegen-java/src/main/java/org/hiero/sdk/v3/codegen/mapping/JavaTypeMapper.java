package org.hiero.sdk.v3.codegen.mapping;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import org.hiero.sdk.v3.codegen.config.CodegenConfiguration;
import org.hiero.sdk.v3.codegen.model.CollectionTypeReference;
import org.hiero.sdk.v3.codegen.model.NamedTypeReference;
import org.hiero.sdk.v3.codegen.model.PrimitiveKind;
import org.hiero.sdk.v3.codegen.model.PrimitiveTypeReference;
import org.hiero.sdk.v3.codegen.model.SchemaElements;
import org.hiero.sdk.v3.codegen.model.TypeReference;
import org.hiero.sdk.v3.codegen.model.TypeVariableReference;

public final class JavaTypeMapper {

    private final String packageRoot;

    public JavaTypeMapper(CodegenConfiguration configuration) {
        this.packageRoot = configuration.packageRoot();
    }

    public String packageName(String namespace) {
        return packageRoot + "." + namespace.toLowerCase(Locale.ROOT);
    }

    public String map(TypeReference type, boolean nullable) {
        return map(type, nullable, false);
    }

    public Set<String> imports(TypeReference type) {
        var imports = new TreeSet<String>();
        collectImports(type, imports);
        return Set.copyOf(imports);
    }

    private String map(TypeReference type, boolean nullable, boolean genericArgument) {
        return switch (type) {
            case PrimitiveTypeReference primitive -> mapPrimitive(primitive.kind(), nullable || genericArgument);
            case NamedTypeReference named -> named.name()
                    + (named.arguments().isEmpty() ? "" : "<" + named.arguments().stream()
                            .map(argument -> map(argument, false, true))
                            .collect(java.util.stream.Collectors.joining(", ")) + ">");
            case TypeVariableReference variable -> variable.name();
            case CollectionTypeReference collection -> {
                var collectionName = collection.kind().name().equals("LIST") ? "List" : "Set";
                yield collectionName + "<" + map(collection.elementType(), false, true) + ">";
            }
            case org.hiero.sdk.v3.codegen.model.WildcardTypeReference ignored -> "?";
        };
    }

    private static String mapPrimitive(PrimitiveKind kind, boolean boxed) {
        return switch (kind) {
            case BOOL -> boxed ? "Boolean" : "boolean";
            case INT32, UINT16 -> boxed ? "Integer" : "int";
            case INT64, UINT64 -> boxed ? "Long" : "long";
            case DOUBLE -> boxed ? "Double" : "double";
            case STRING -> "String";
            case BYTES -> "byte[]";
            case ZONED_DATE_TIME -> "ZonedDateTime";
            case VOID -> "void";
        };
    }

    private static void collectImports(TypeReference type, Set<String> imports) {
        switch (type) {
            case PrimitiveTypeReference primitive -> {
                if (primitive.kind() == PrimitiveKind.ZONED_DATE_TIME) {
                    imports.add("java.time.ZonedDateTime");
                }
            }
            case CollectionTypeReference collection -> {
                imports.add(collection.kind().name().equals("LIST") ? "java.util.List" : "java.util.Set");
                collectImports(collection.elementType(), imports);
            }
            case NamedTypeReference named -> named.arguments().forEach(argument -> collectImports(argument, imports));
            case TypeVariableReference ignored -> {
            }
            case org.hiero.sdk.v3.codegen.model.WildcardTypeReference ignored -> {
            }
        }
    }

    public String sourceIdentity(TypeReference type) {
        return SchemaElements.display(type);
    }
}
