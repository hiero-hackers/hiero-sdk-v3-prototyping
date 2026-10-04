package org.hiero.sdk.v3.metalang.generator.rust;

import java.math.BigDecimal;
import java.util.Set;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.IntegerRange;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * The checks of constructors and setters: the validation annotations and the range of integer types that Rust has no
 * exact type for (e.g. {@code int24} in an {@code i32}). A violated check returns an {@code InvalidArgumentError}.
 */
final class RustConstraints {

    /** The annotations that a constructor or setter checks. */
    static final Set<String> VALIDATIONS = Set.of("min", "max", "minLength", "maxLength", "minSize", "maxSize",
            "pattern", "urlPattern");

    private RustConstraints() {
    }

    /** Whether a constructor or setter of the attribute can fail. */
    static boolean isChecked(final FieldDefinition field) {
        return field.annotations().stream().anyMatch(a -> VALIDATIONS.contains(a.name()))
                || IntegerRange.integer(field.type()).filter(RustContext::needsRangeCheck).isPresent();
    }

    /**
     * Returns the checks of an attribute.
     *
     * @param field   the attribute
     * @param name    the variable that holds the value (owned, an {@code Option} if the attribute is nullable)
     * @param indent  the indentation of the statements
     * @param imports the imports of the file
     * @return the statements, each ending with a line break; empty if there is nothing to check
     */
    static String checks(final FieldDefinition field, final String name, final String indent,
                         final RustImports imports) {
        final boolean nullable = field.hasAnnotation("nullable");
        // in an `if let Some(name) = &name` the value is a reference
        final String value = nullable ? "*" + name : name;
        final String reference = nullable ? name : "&" + name;
        final StringBuilder checks = new StringBuilder();
        final String error = imports.support("InvalidArgumentError");
        IntegerRange.integer(field.type()).filter(RustContext::needsRangeCheck).ifPresent(builtin -> {
            final IntegerRange range = IntegerRange.of(builtin);
            check(checks, "!(" + literal(range.min().toString(), builtin) + "..=" + literal(range.max().toString(),
                    builtin) + ").contains(" + reference + ")", field.name() + " must be an integer between "
                    + range.min() + " and " + range.max(), error);
        });
        for (final Annotation annotation : field.annotations()) {
            if ((annotation.name().equals("minSize") || annotation.name().equals("minLength"))
                    && number(annotation).equals("0")) {
                continue; // every value has at least 0 elements
            }
            switch (annotation.name()) {
                case "min" -> check(checks, value + " < " + bound(field.type(), annotation),
                        field.name() + " must be at least " + number(annotation), error);
                case "max" -> check(checks, value + " > " + bound(field.type(), annotation),
                        field.name() + " must be at most " + number(annotation), error);
                case "minLength" -> check(checks, number(annotation).equals("1") ? name + ".is_empty()"
                                : name + ".chars().count() < " + number(annotation),
                        field.name() + " must be at least " + number(annotation) + " characters long", error);
                case "maxLength" -> check(checks, name + ".chars().count() > " + number(annotation),
                        field.name() + " must be at most " + number(annotation) + " characters long", error);
                case "minSize" -> check(checks, number(annotation).equals("1") ? name + ".is_empty()"
                                : name + ".len() < " + number(annotation),
                        field.name() + " must contain at least " + number(annotation) + " element(s)", error);
                case "maxSize" -> check(checks, name + ".len() > " + number(annotation),
                        field.name() + " must contain at most " + number(annotation) + " element(s)", error);
                case "pattern" -> {
                    final String pattern = string(annotation);
                    checks.append("static PATTERN: std::sync::LazyLock<regex::Regex> = std::sync::LazyLock::new(|| "
                            + "regex::Regex::new(").append(RustLiterals.quote(pattern))
                            .append(").expect(\"valid pattern\"));\n");
                    check(checks, "!PATTERN.is_match(" + reference + ")", field.name() + " must match the pattern "
                            + pattern, error);
                }
                case "urlPattern" -> check(checks, "!" + imports.support("is_absolute_url") + "(" + reference + ")",
                        field.name() + " must be an absolute URL with a host", error);
                default -> {
                }
            }
        }
        if (checks.isEmpty()) {
            return "";
        }
        final StringBuilder out = new StringBuilder();
        if (nullable) {
            out.append(indent).append("if let Some(").append(name).append(") = &").append(name).append(" {\n");
            checks.toString().lines().forEach(l -> out.append(indent).append("    ").append(l).append('\n'));
            out.append(indent).append("}\n");
        } else if (checks.indexOf("static PATTERN") >= 0) {
            out.append(indent).append("{\n");
            checks.toString().lines().forEach(l -> out.append(indent).append("    ").append(l).append('\n'));
            out.append(indent).append("}\n");
        } else {
            checks.toString().lines().forEach(l -> out.append(indent).append(l).append('\n'));
        }
        return out.toString();
    }

    private static void check(final StringBuilder out, final String condition, final String message,
                              final String error) {
        out.append("if ").append(condition).append(" {\n").append("    return Err(").append(error).append("::new(")
                .append(RustLiterals.quote(message)).append("));\n").append("}\n");
    }

    private static String bound(final Type type, final Annotation annotation) {
        if (type instanceof Type.BasicType basic) {
            return switch (basic.builtin().category()) {
                case INTEGER -> literal(number(annotation), basic.builtin());
                case FLOAT, DECIMAL, DURATION -> RustLiterals.number(new BigDecimal(number(annotation)),
                        basic.builtin());
                default -> throw new IllegalArgumentException("@@" + annotation.name() + " on " + type.text());
            };
        }
        throw new IllegalArgumentException("@@" + annotation.name() + " on " + type.text());
    }

    private static String literal(final String value, final BuiltinType builtin) {
        return RustLiterals.number(new BigDecimal(value), builtin);
    }

    static String number(final Annotation annotation) {
        return annotation.arguments().getFirst().text().replace("_", "");
    }

    private static String string(final Annotation annotation) {
        return annotation.arguments().getFirst() instanceof Literal.StringLiteral literal ? literal.value()
                : annotation.arguments().getFirst().text();
    }
}
