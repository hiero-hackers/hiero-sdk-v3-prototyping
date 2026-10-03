package org.hiero.sdk.v3.metalang.generator.ts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Renders meta-language literals (values of constants, {@code @@default}, enum attributes) as TypeScript expressions.
 */
final class TsLiterals {

    private TsLiterals() {
    }

    /**
     * Renders a literal for a type.
     *
     * @param literal the literal
     * @param type    the type of the value
     * @param imports the imports of the file
     * @return the expression
     * @throws IllegalArgumentException if the literal has no TypeScript form for the type
     */
    static String expression(final Literal literal, final Type type, final TsImports imports) {
        if (literal instanceof Literal.NameLiteral name && name.text().equals("null")) {
            return "null";
        }
        return switch (type) {
            case Type.BasicType basic -> basic(literal, basic, imports);
            case Type.DeclaredType declared when literal instanceof Literal.NameLiteral name ->
                    imports.value(declared.name()) + "." + name.text().substring(name.text().lastIndexOf('.') + 1);
            case Type.DeclaredType declared when literal instanceof Literal.StructLiteral struct ->
                    struct(struct, declared, imports);
            default -> throw new IllegalArgumentException("no TypeScript form of " + literal.text() + " for "
                    + type.text());
        };
    }

    private static String struct(final Literal.StructLiteral struct, final Type.DeclaredType type,
                                 final TsImports imports) {
        final TypeDefinition definition = imports.context().model().definition(type);
        final List<String> values = new ArrayList<>();
        for (final FieldDefinition field : definition.fields()) {
            final Optional<Literal> value = struct.entries().stream().filter(e -> e.name().equals(field.name()))
                    .map(Literal.StructEntry::value).findFirst();
            value.ifPresent(v -> values.add(field.name() + ": " + expression(v,
                    imports.context().model().substitute(type, field.type()), imports)));
        }
        return "new " + imports.value(type.name()) + "({ " + String.join(", ", values) + " })";
    }

    private static String basic(final Literal literal, final Type.BasicType type, final TsImports imports) {
        final BuiltinType builtin = type.builtin();
        return switch (literal) {
            case Literal.StringLiteral string -> quote(string.value());
            case Literal.NameLiteral name -> name.text(); // true / false
            case Literal.NumberLiteral number -> number(number.text().replace("_", ""), builtin, imports);
            case Literal.ListLiteral list -> switch (builtin.category()) {
                case BYTES -> "new Uint8Array([" + list.items().stream().map(Literal::text)
                        .collect(Collectors.joining(", ")) + "])";
                case MAP -> "new Map()";
                default -> {
                    final String items = list.items().stream()
                            .map(i -> expression(i, type.arguments().getFirst(), imports))
                            .collect(Collectors.joining(", "));
                    yield builtin.name().equals("set") ? "new Set([" + items + "])"
                            : "Object.freeze([" + items + "])";
                }
            };
            case Literal.StructLiteral struct -> throw new IllegalArgumentException(struct.text());
        };
    }

    /**
     * Renders a number for a type: {@code 5}, {@code 5n} for {@code bigint}, {@code "1.5"} for {@code decimal},
     * {@code Duration.ofSeconds(5)}.
     *
     * @param text    the number
     * @param builtin the type
     * @param imports the imports of the file
     * @return the expression
     */
    static String number(final String text, final BuiltinType builtin, final TsImports imports) {
        return switch (builtin.category()) {
            case INTEGER -> TsTypes.isBigInt(builtin) ? text + "n" : text;
            case DECIMAL -> quote(text);
            case DURATION -> imports.support("Duration", true)
                    + (builtin.name().equals("seconds") ? ".ofSeconds(" : ".ofMillis(") + text + ")";
            default -> text;
        };
    }

    /**
     * Quotes a string as TypeScript string literal.
     *
     * @param value the value
     * @return the literal
     */
    static String quote(final String value) {
        final StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20 || c == 0x2028 || c == 0x2029) {
                        out.append(String.format(java.util.Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
