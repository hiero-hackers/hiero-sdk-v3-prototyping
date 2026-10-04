package org.hiero.sdk.v3.metalang.generator.rust;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/** Renders literals of the specs (constants, defaults, enum attributes) as Rust expressions. */
final class RustLiterals {

    private RustLiterals() {
    }

    /**
     * Renders a literal as an owned value of a type.
     *
     * @param literal  the literal
     * @param type     the meta-language type (without type variables)
     * @param nullable whether the value is optional ({@code Some(..)} / {@code None})
     * @param imports  the imports of the file
     * @return the Rust expression
     */
    static String expression(final Literal literal, final Type type, final boolean nullable,
                             final RustImports imports) {
        if (literal instanceof Literal.NameLiteral name && name.text().equals("null")) {
            return "None";
        }
        final String value = value(literal, type, imports);
        return nullable ? "Some(" + value + ")" : value;
    }

    private static String value(final Literal literal, final Type type, final RustImports imports) {
        return switch (type) {
            case Type.BasicType basic -> basic(literal, basic, imports);
            case Type.DeclaredType declared when literal instanceof Literal.NameLiteral name ->
                    enumConstant(declared, name.text(), imports);
            case Type.DeclaredType declared when literal instanceof Literal.StructLiteral struct ->
                    struct(struct, declared, imports);
            default -> throw new IllegalArgumentException("no Rust form of " + literal.text() + " for "
                    + type.text());
        };
    }

    /** {@code Kind.A} or {@code A} as {@code Kind::A}. */
    static String enumConstant(final Type.DeclaredType type, final String text, final RustImports imports) {
        return imports.type(type.name()) + "::" + RustNames.variant(text.substring(text.lastIndexOf('.') + 1));
    }

    private static String struct(final Literal.StructLiteral struct, final Type.DeclaredType type,
                                 final RustImports imports) {
        final RustContext context = imports.context();
        final TypeDefinition definition = context.model().definition(type);
        final List<String> values = new ArrayList<>();
        for (final FieldDefinition field : definition.fields()) {
            final Optional<Literal> value = struct.entries().stream().filter(e -> e.name().equals(field.name()))
                    .map(Literal.StructEntry::value).findFirst();
            final Type fieldType = context.model().substitute(type, field.type());
            final boolean nullable = field.hasAnnotation("nullable");
            if (value.isPresent()) {
                values.add(expression(value.get(), fieldType, nullable, imports));
            } else if (field.hasAnnotation("default")) {
                values.add(expression(field.annotation("default").orElseThrow().arguments().getFirst(), fieldType,
                        nullable, imports));
            } else {
                values.add("None");
            }
        }
        return imports.type(type.name()) + "::new(" + String.join(", ", values) + ")"
                + (context.isFallible(type.name()) ? ".expect(\"valid value\")" : "");
    }

    private static String basic(final Literal literal, final Type.BasicType type, final RustImports imports) {
        final BuiltinType builtin = type.builtin();
        return switch (literal) {
            case Literal.StringLiteral string -> quote(string.value()) + ".to_string()";
            case Literal.NameLiteral name -> name.text(); // true / false
            case Literal.NumberLiteral number -> number(number.value(), builtin);
            case Literal.ListLiteral list -> switch (builtin.category()) {
                case BYTES -> "vec![" + list.items().stream().map(Literal::text).collect(Collectors.joining(", "))
                        + "]";
                case MAP -> imports.external("std::collections::HashMap") + "::new()";
                default -> {
                    final String items = list.items().stream()
                            .map(i -> expression(i, type.arguments().getFirst(), false, imports))
                            .collect(Collectors.joining(", "));
                    yield builtin.name().equals("set") && imports.context().rustType(type, Map.of())
                            instanceof RustType.SetOf ? imports.external("std::collections::HashSet") + "::from(["
                            + items + "])" : "vec![" + items + "]";
                }
            };
            case Literal.StructLiteral struct -> throw new IllegalArgumentException(struct.text());
        };
    }

    /**
     * Renders a number as a value of a numeric type.
     *
     * @param value   the number
     * @param builtin the type: integer, {@code double}, {@code decimal}, {@code seconds} or {@code duration}
     * @return the expression; for the types of the standard library and {@code rust_decimal} a constant expression
     */
    static String number(final BigDecimal value, final BuiltinType builtin) {
        return switch (builtin.category()) {
            case INTEGER -> {
                final String type = RustContext.integer(builtin);
                if (type.startsWith("ethnum::")) {
                    yield type + "::new(" + value.toBigInteger() + ")";
                }
                yield value.toBigInteger().toString();
            }
            case FLOAT -> {
                final String text = value.toPlainString();
                yield text.contains(".") ? text : text + ".0";
            }
            case DECIMAL -> "rust_decimal::Decimal::from_i128_with_scale(" + value.unscaledValue() + ", "
                    + Math.max(0, value.scale()) + ")";
            case DURATION -> builtin.name().equals("seconds")
                    ? "std::time::Duration::from_millis(" + value.multiply(BigDecimal.valueOf(1000)).longValue() + ")"
                    : "std::time::Duration::from_millis(" + value.longValue() + ")";
            default -> value.toPlainString();
        };
    }

    /** The literal of an enum attribute value: a constant expression ({@code &'static str} for strings). */
    static String constant(final Literal literal, final Type type, final RustImports imports) {
        if (literal instanceof Literal.StringLiteral string) {
            return quote(string.value());
        }
        if (type instanceof Type.DeclaredType declared && literal instanceof Literal.NameLiteral name) {
            return enumConstant(declared, name.text(), imports);
        }
        if (type instanceof Type.BasicType basic && literal instanceof Literal.NumberLiteral number) {
            return number(number.value(), basic.builtin());
        }
        return literal.text();
    }

    /**
     * Quotes a string as Rust string literal.
     *
     * @param value the string
     * @return the literal with quotes
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
                    if (c < 0x20) {
                        out.append(String.format(java.util.Locale.ROOT, "\\u{%x}", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
