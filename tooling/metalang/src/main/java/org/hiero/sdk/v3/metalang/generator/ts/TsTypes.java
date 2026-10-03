package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Maps meta-language types to TypeScript types (see "Type Mapping" in {@code guidelines/api-best-practices-ts.md}).
 *
 * <ul>
 *   <li>Integers up to 32 bits ({@code int8} to {@code int32}, {@code uint8} to {@code uint32}) are {@code number},
 *       wider ones {@code bigint}; {@code double} is {@code number}, {@code decimal} a decimal {@code string}.</li>
 *   <li>{@code bytes} is {@code Uint8Array}, {@code list<T>} {@code ReadonlyArray<T>}, {@code set<T>}
 *       {@code ReadonlySet<T>}, {@code map<K, V>} {@code ReadonlyMap<K, V>}.</li>
 *   <li>Time types are {@code Date}, {@code duration}/{@code seconds} the support class {@code Duration},
 *       {@code type<T>} {@code AbstractConstructor<T>}, {@code streamResult<T>} {@code StreamItem<T>}, {@code uuid}
 *       {@code string}, {@code ANY} {@code unknown}.</li>
 *   <li>Function types are function types ({@code (event: Event) => void}); {@code @@nullable} is {@code | null}
 *       ({@code undefined} is never part of the API).</li>
 * </ul>
 */
final class TsTypes {

    private TsTypes() {
    }

    /**
     * Returns the TypeScript type including {@code | null} for a nullable declaration.
     *
     * @param type     the type
     * @param nullable whether the declaration is {@code @@nullable}
     * @param imports  the imports of the file
     * @return the type
     */
    static String declaration(final Type type, final boolean nullable, final TsImports imports) {
        final String ts = type(type, imports);
        if (!nullable) {
            return ts;
        }
        return (type instanceof Type.FunctionType ? "(" + ts + ")" : ts) + " | null";
    }

    /**
     * Returns the TypeScript type.
     *
     * @param type    the type
     * @param imports the imports of the file
     * @return the type
     */
    static String type(final Type type, final TsImports imports) {
        return switch (type) {
            case Type.BasicType basic -> basic(basic, imports);
            case Type.DeclaredType declared -> imports.type(declared.name()) + arguments(declared, imports);
            case Type.TypeVariable variable -> imports.context().typeVariable(variable.name());
            case Type.WildcardType wildcard -> wildcard.upperBound() == null ? "unknown"
                    : type(wildcard.upperBound(), imports);
            case Type.AnyType ignored -> "unknown";
            case Type.VoidType ignored -> "void";
            case Type.FunctionType function -> "(" + parameters(function.parameters(), imports) + ") => "
                    + type(function.returnType(), imports);
            case Type.UnresolvedType unresolved -> throw new IllegalStateException("unresolved " + unresolved.text());
        };
    }

    /** Whether a meta-language integer type is a {@code bigint} in TypeScript. */
    static boolean isBigInt(final BuiltinType builtin) {
        return builtin.category() == BuiltinType.Category.INTEGER && builtin.bits() > 32;
    }

    private static String basic(final Type.BasicType basic, final TsImports imports) {
        final BuiltinType builtin = basic.builtin();
        final List<Type> arguments = basic.arguments();
        return switch (builtin.category()) {
            case INTEGER -> isBigInt(builtin) ? "bigint" : "number";
            case FLOAT -> "number";
            case DECIMAL, STRING, UUID -> "string";
            case BOOL -> "boolean";
            case BYTES -> "Uint8Array";
            case COLLECTION -> (builtin.name().equals("set") ? "ReadonlySet<" : "ReadonlyArray<")
                    + type(arguments.getFirst(), imports) + ">";
            case MAP -> "ReadonlyMap<" + type(arguments.get(0), imports) + ", " + type(arguments.get(1), imports)
                    + ">";
            case TYPE -> imports.support("AbstractConstructor", false) + "<"
                    + (arguments.isEmpty() ? "unknown" : type(arguments.getFirst(), imports)) + ">";
            case TEMPORAL -> "Date";
            case DURATION -> imports.support("Duration", false);
            case STREAM_RESULT -> imports.support("StreamItem", false) + "<" + type(arguments.getFirst(), imports)
                    + ">";
        };
    }

    /**
     * The type arguments of a declared type. {@code ANY} as argument becomes the bound of the type parameter (or
     * {@code unknown} without bound, {@code any} if the bound refers to type parameters, e.g. a self type), because
     * TypeScript checks the bound of every argument.
     */
    private static String arguments(final Type.DeclaredType declared, final TsImports imports) {
        if (declared.arguments().isEmpty()) {
            return "";
        }
        final List<TypeParameterDefinition> parameters = imports.context().model().type(declared.name())
                .map(TypeDefinition::typeParameters).orElse(List.of());
        final List<String> result = new ArrayList<>();
        for (int i = 0; i < declared.arguments().size(); i++) {
            final Type argument = declared.arguments().get(i);
            final TypeParameterDefinition parameter = i < parameters.size() ? parameters.get(i) : null;
            if ((argument instanceof Type.AnyType || argument instanceof Type.WildcardType w
                    && w.upperBound() == null) && parameter != null && parameter.bound() != null) {
                result.add(containsVariable(parameter.bound()) ? "any" : type(parameter.bound(), imports));
            } else {
                result.add(type(argument, imports));
            }
        }
        return "<" + String.join(", ", result) + ">";
    }

    /**
     * The parameter list of a method or function type, e.g. {@code payer: Account, nodes: ReadonlyArray<AccountId>}.
     *
     * @param parameters the parameters
     * @param imports    the imports of the file
     * @return the parameter list
     */
    static String parameters(final List<ParameterDefinition> parameters, final TsImports imports) {
        return parameters.stream().map(p -> p.varargs()
                        ? "..." + TsNames.local(p.name()) + ": " + arrayOf(type(p.type(), imports))
                        : TsNames.local(p.name()) + ": " + declaration(p.type(), p.hasAnnotation("nullable"), imports))
                .collect(Collectors.joining(", "));
    }

    private static String arrayOf(final String element) {
        return element.matches("[\\w.<>, ]+") ? element + "[]" : "Array<" + element + ">";
    }

    static boolean containsVariable(final Type type) {
        return switch (type) {
            case Type.TypeVariable ignored -> true;
            case Type.BasicType basic -> basic.arguments().stream().anyMatch(TsTypes::containsVariable);
            case Type.DeclaredType declared -> declared.arguments().stream().anyMatch(TsTypes::containsVariable);
            case Type.WildcardType wildcard -> wildcard.upperBound() != null && containsVariable(wildcard.upperBound());
            case Type.FunctionType function -> containsVariable(function.returnType())
                    || function.parameters().stream().anyMatch(p -> containsVariable(p.type()));
            default -> false;
        };
    }

    /**
     * The type parameters of a declaration, e.g. {@code <ReceiptT extends Receipt, Self extends Transaction<...>>}.
     *
     * @param parameters the type parameters
     * @param imports    the imports of the file
     * @return the type parameter list, empty if there are none
     */
    static String typeParameters(final List<TypeParameterDefinition> parameters, final TsImports imports) {
        if (parameters.isEmpty()) {
            return "";
        }
        return "<" + parameters.stream().map(p -> imports.context().typeVariable(p.name())
                        + (p.bound() == null ? "" : " extends " + type(p.bound(), imports)))
                .collect(Collectors.joining(", ")) + ">";
    }
}
