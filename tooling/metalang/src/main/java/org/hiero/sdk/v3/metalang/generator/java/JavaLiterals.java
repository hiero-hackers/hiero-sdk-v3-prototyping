package org.hiero.sdk.v3.metalang.generator.java;

import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Converts meta-language literals into Java expressions of a given target type (used for enum attribute values).
 * The validator guarantees that the literal fits the type.
 */
final class JavaLiterals {

    private JavaLiterals() {
    }

    /**
     * Returns the Java expression for a literal.
     *
     * @param literal the literal
     * @param type    the target type
     * @param imports the imports of the file
     * @return the Java expression
     * @throws JavaTypes.UnsupportedTypeException if the literal has no Java form yet
     */
    static String expression(final Literal literal, final Type type, final Imports imports) {
        if (literal instanceof Literal.NameLiteral name && name.text().equals("null")) {
            return "null";
        }
        return switch (type) {
            case Type.BasicType basic -> basic(literal, basic, imports);
            case Type.DeclaredType declared when literal instanceof Literal.NameLiteral name ->
                    JavaTypes.type(declared, false, imports) + "."
                            + name.text().substring(name.text().lastIndexOf('.') + 1);
            default -> throw new JavaTypes.UnsupportedTypeException(type.text() + " = " + literal.text());
        };
    }

    private static String basic(final Literal literal, final Type.BasicType type, final Imports imports) {
        final BuiltinType builtin = type.builtin();
        return switch (literal) {
            case Literal.StringLiteral string -> quote(string.value());
            case Literal.NameLiteral name -> name.text(); // true / false
            case Literal.NumberLiteral number -> number(number, builtin, imports);
            case Literal.ListLiteral list -> switch (builtin.category()) {
                case BYTES -> list.items().isEmpty() ? "new byte[0]" : "new byte[] {" + list.items().stream()
                        .map(i -> "(byte) " + i.text()).collect(Collectors.joining(", ")) + "}";
                case MAP -> imports.use("java.util", "Map") + ".of()";
                default -> imports.use("java.util", builtin.name().equals("set") ? "Set" : "List") + ".of("
                        + list.items().stream()
                        .map(i -> expression(i, type.arguments().getFirst(), imports))
                        .collect(Collectors.joining(", ")) + ")";
            };
            case Literal.StructLiteral struct -> throw new JavaTypes.UnsupportedTypeException(struct.text());
        };
    }

    private static String number(final Literal.NumberLiteral number, final BuiltinType builtin,
                                 final Imports imports) {
        final String text = number.text();
        return switch (builtin.category()) {
            case INTEGER -> {
                if (JavaTypes.javaBits(builtin) <= 8) {
                    yield "(byte) " + text;
                } else if (JavaTypes.javaBits(builtin) <= 16) {
                    yield "(short) " + text;
                } else if (JavaTypes.javaBits(builtin) <= 32) {
                    yield text;
                } else if (JavaTypes.javaBits(builtin) <= 64) {
                    yield text + "L";
                }
                yield "new " + imports.use("java.math", "BigInteger") + "(\"" + text.replace("_", "") + "\")";
            }
            case DECIMAL -> "new " + imports.use("java.math", "BigDecimal") + "(\"" + text.replace("_", "") + "\")";
            case DURATION -> imports.use("java.time", "Duration")
                    + (builtin.name().equals("seconds") ? ".ofSeconds(" : ".ofMillis(") + text + "L)";
            default -> text.contains(".") ? text : text + ".0"; // double
        };
    }

    /**
     * Returns a Java string literal.
     *
     * @param value the string value
     * @return the quoted and escaped literal
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
                default -> out.append(c);
            }
        }
        return out.append('"').toString();
    }
}
