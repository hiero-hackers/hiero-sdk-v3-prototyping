package org.hiero.sdk.v3.metalang.generator.ts;

import java.math.BigInteger;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.IntegerRange;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Renders the checks of an attribute as TypeScript statements: {@code null} for a non-nullable attribute throws a
 * {@code TypeError}; the range of an integer type (JavaScript numbers and {@code bigint} accept any value, so every
 * integer attribute is checked, a {@code number} also to be an integer) and the validation annotations
 * ({@code @@min}, {@code @@max}, {@code @@minLength}, {@code @@maxLength}, {@code @@minSize}, {@code @@maxSize},
 * {@code @@pattern}, {@code @@urlPattern}) throw a {@code RangeError}. A {@code null} value of a nullable attribute is
 * not checked.
 */
final class TsConstraints {

    private TsConstraints() {
    }

    /**
     * Returns the checks of an attribute.
     *
     * @param field   the attribute
     * @param value   the expression of the value
     * @param indent  the indentation of the statements
     * @param imports the imports of the file
     * @return the statements, each ending with a line break
     */
    static String checks(final FieldDefinition field, final String value, final String indent,
                         final TsImports imports) {
        final boolean nullable = field.hasAnnotation("nullable");
        final StringBuilder out = new StringBuilder();
        if (!nullable) {
            out.append(indent).append("if (").append(value).append(" === null || ").append(value)
                    .append(" === undefined) {\n").append(indent).append("    throw new TypeError(\"").append(field.name())
                    .append(" must not be null\");\n").append(indent).append("}\n");
        }
        final StringBuilder checks = new StringBuilder();
        IntegerRange.integer(field.type()).ifPresent(builtin -> {
            final IntegerRange range = IntegerRange.of(builtin);
            final String condition = TsTypes.isBigInt(builtin)
                    ? value + " < " + range.min() + "n || " + value + " > " + range.max() + "n"
                    : "!Number.isInteger(" + value + ") || " + value + " < " + range.min() + " || " + value + " > "
                    + range.max();
            check(checks, condition, field.name() + " must be an integer between " + range.min() + " and "
                    + range.max());
        });
        for (final Annotation annotation : field.annotations()) {
            switch (annotation.name()) {
                case "min" -> check(checks, compare(field.type(), value, annotation, "<", imports),
                        field.name() + " must be at least " + number(annotation));
                case "max" -> check(checks, compare(field.type(), value, annotation, ">", imports),
                        field.name() + " must be at most " + number(annotation));
                case "minLength" -> check(checks, value + ".length < " + number(annotation),
                        field.name() + " must be at least " + number(annotation) + " characters long");
                case "maxLength" -> check(checks, value + ".length > " + number(annotation),
                        field.name() + " must be at most " + number(annotation) + " characters long");
                case "minSize" -> check(checks, size(field.type(), value) + " < " + number(annotation),
                        field.name() + " must contain at least " + number(annotation) + " element(s)");
                case "maxSize" -> check(checks, size(field.type(), value) + " > " + number(annotation),
                        field.name() + " must contain at most " + number(annotation) + " element(s)");
                case "pattern" -> check(checks, "!new RegExp(" + TsLiterals.quote(string(annotation)) + ").test("
                        + value + ")", field.name() + " must match the pattern " + string(annotation));
                case "urlPattern" -> check(checks, "!isAbsoluteUrl(" + value + ")",
                        field.name() + " must be an absolute URL with a host");
                default -> {
                }
            }
        }
        if (!checks.isEmpty()) {
            if (nullable) {
                out.append(indent).append("if (").append(value).append(" !== null) {\n");
                checks.toString().lines().forEach(l -> out.append(indent).append("    ").append(l).append('\n'));
                out.append(indent).append("}\n");
            } else {
                checks.toString().lines().forEach(l -> out.append(indent).append(l).append('\n'));
            }
        }
        return out.toString();
    }

    /** Whether the checks of the attributes need the {@code isAbsoluteUrl} helper of the file. */
    static boolean needsUrlCheck(final FieldDefinition field) {
        return field.hasAnnotation("urlPattern");
    }

    /** The helper function that checks an absolute URL with a host, added once to a file that needs it. */
    static String urlHelper() {
        return """
                function isAbsoluteUrl(value: string): boolean {
                    try {
                        return new URL(value).host !== "";
                    } catch {
                        return false;
                    }
                }
                """;
    }

    private static void check(final StringBuilder out, final String condition, final String message) {
        out.append("if (").append(condition).append(") {\n").append("    throw new RangeError(")
                .append(TsLiterals.quote(message)).append(");\n").append("}\n");
    }

    private static String compare(final Type type, final String value, final Annotation annotation,
                                  final String operator, final TsImports imports) {
        final String bound = number(annotation);
        if (type instanceof Type.BasicType basic) {
            return switch (basic.builtin().category()) {
                case INTEGER -> TsTypes.isBigInt(basic.builtin())
                        ? value + " " + operator + " " + new BigInteger(bound) + "n"
                        : value + " " + operator + " " + bound;
                case FLOAT -> value + " " + operator + " " + bound;
                case DECIMAL -> "Number(" + value + ") " + operator + " " + bound;
                case DURATION -> value + ".toMillis() " + operator + " "
                        + TsLiterals.number(bound, basic.builtin(), imports) + ".toMillis()";
                default -> throw new IllegalArgumentException("@@" + annotation.name() + " on " + type.text());
            };
        }
        throw new IllegalArgumentException("@@" + annotation.name() + " on " + type.text());
    }

    private static String size(final Type type, final String value) {
        return type instanceof Type.BasicType basic && (basic.builtin().category() == BuiltinType.Category.BYTES
                || basic.builtin().name().equals("list")) ? value + ".length" : value + ".size";
    }

    private static String number(final Annotation annotation) {
        return annotation.arguments().getFirst().text().replace("_", "");
    }

    private static String string(final Annotation annotation) {
        return annotation.arguments().getFirst() instanceof Literal.StringLiteral literal ? literal.value()
                : annotation.arguments().getFirst().text();
    }
}
