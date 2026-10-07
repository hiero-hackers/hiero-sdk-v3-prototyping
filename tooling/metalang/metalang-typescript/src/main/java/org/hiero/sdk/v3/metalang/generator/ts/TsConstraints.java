package org.hiero.sdk.v3.metalang.generator.ts;

import java.math.BigInteger;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.IntegerRange;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Renders the checks of an attribute as TypeScript statements: {@code null} for a non-nullable attribute and a
 * value of the wrong runtime type throw a {@code TypeError}; the range of an integer type (JavaScript numbers and
 * {@code bigint} accept any value, so every integer attribute is checked, a {@code number} also to be an integer)
 * and the validation annotations
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
        // the type first: a range comparison coerces, so "1001" would pass the check below as a uint64
        typeCheck(field.type(), value, imports).ifPresent(check ->
                check(checks, check.condition(), field.name() + " must be " + check.description(), "TypeError"));
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
        check(out, condition, message, "RangeError");
    }

    private static void check(final StringBuilder out, final String condition, final String message,
                              final String errorType) {
        out.append("if (").append(condition).append(") {\n").append("    throw new ").append(errorType).append('(')
                .append(TsLiterals.quote(message)).append(");\n").append("}\n");
    }

    /**
     * A runtime type check: the condition that is true for a wrong value, and how the type reads in the message.
     *
     * @param condition   the condition
     * @param description the description, e.g. {@code a bigint}
     */
    private record TypeCheck(String condition, String description) {
    }

    /**
     * The runtime type check of an attribute, if its type has one.
     *
     * <p>Only the built-in types are checked. A declared type is deliberately left out: TypeScript is structurally
     * typed and the guideline asks for interface types, so an {@code instanceof} against the generated class would
     * reject a value that satisfies the type but was not built by this SDK.
     *
     * @param type    the attribute type
     * @param value   the expression of the value
     * @param imports the imports of the file
     * @return the check, or empty if the type has none
     */
    private static Optional<TypeCheck> typeCheck(final Type type, final String value, final TsImports imports) {
        if (type instanceof Type.FunctionType) {
            return Optional.of(new TypeCheck("typeof " + value + " !== \"function\"", "a function"));
        }
        if (!(type instanceof Type.BasicType basic)) {
            return Optional.empty();
        }
        final BuiltinType builtin = basic.builtin();
        return switch (builtin.category()) {
            case INTEGER -> Optional.of(TsTypes.isBigInt(builtin)
                    ? new TypeCheck("typeof " + value + " !== \"bigint\"", "a bigint")
                    : new TypeCheck("typeof " + value + " !== \"number\"", "a number"));
            case FLOAT -> Optional.of(new TypeCheck("typeof " + value + " !== \"number\"", "a number"));
            case DECIMAL, STRING, UUID ->
                    Optional.of(new TypeCheck("typeof " + value + " !== \"string\"", "a string"));
            case BOOL -> Optional.of(new TypeCheck("typeof " + value + " !== \"boolean\"", "a boolean"));
            case BYTES -> Optional.of(instanceCheck(value, "Uint8Array"));
            case COLLECTION -> Optional.of(builtin.name().equals("set") ? instanceCheck(value, "Set")
                    : new TypeCheck("!Array.isArray(" + value + ")", "an array"));
            case MAP -> Optional.of(instanceCheck(value, "Map"));
            case TEMPORAL -> Optional.of(instanceCheck(value, "Date"));
            case DURATION -> Optional.of(instanceCheck(value, imports.support("Duration", true)));
            case TYPE -> Optional.of(new TypeCheck("typeof " + value + " !== \"function\"", "a class"));
            // a StreamItem is a plain object; there is nothing to check it against
            case STREAM_RESULT -> Optional.empty();
        };
    }

    private static TypeCheck instanceCheck(final String value, final String className) {
        return new TypeCheck("!(" + value + " instanceof " + className + ")", "a " + className);
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
