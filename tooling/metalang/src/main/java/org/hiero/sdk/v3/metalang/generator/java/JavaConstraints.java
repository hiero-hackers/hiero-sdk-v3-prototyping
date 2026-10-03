package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Renders the checks of the validation annotations of an attribute ({@code @@min}, {@code @@max},
 * {@code @@minLength}, {@code @@maxLength}, {@code @@minSize}, {@code @@maxSize}, {@code @@pattern},
 * {@code @@urlPattern}) as Java statements that throw {@link IllegalArgumentException} (see "Implementation of
 * Attribute annotations" in {@code guidelines/api-best-practices-java.md}). A {@code null} value of a nullable
 * attribute is not checked.
 */
final class JavaConstraints {

    /**
     * The statements of one attribute.
     *
     * @param statements the statements of the constructor (indented, each ending with a line break)
     * @param constants  the {@code private static final} declarations the statements use
     */
    record Checks(String statements, List<String> constants) {
    }

    private JavaConstraints() {
    }

    /**
     * Returns the checks of the attribute.
     *
     * @param field   the attribute
     * @param indent  the indentation of the statements
     * @param imports the imports of the file
     * @return the checks (empty if the attribute has no validation annotation)
     * @throws JavaTypes.UnsupportedTypeException if an annotation cannot be checked for the attribute's type
     */
    static Checks of(final FieldDefinition field, final String indent, final Imports imports) {
        final String name = JavaKeywords.identifier(field.name());
        final boolean nullable = field.hasAnnotation("nullable");
        final StringBuilder out = new StringBuilder();
        final List<String> constants = new ArrayList<>();
        for (final Annotation annotation : field.annotations()) {
            final String condition = switch (annotation.name()) {
                case "min" -> compare(field, name, annotation, "<", imports);
                case "max" -> compare(field, name, annotation, ">", imports);
                case "minLength" -> name + ".length() < " + number(annotation);
                case "maxLength" -> name + ".length() > " + number(annotation);
                case "minSize" -> size(field, name) + " < " + number(annotation);
                case "maxSize" -> size(field, name) + " > " + number(annotation);
                case "pattern" -> {
                    final String constant = constantName(field.name()) + "_PATTERN";
                    constants.add("private static final " + imports.use("java.util.regex", "Pattern") + " "
                            + constant + " = Pattern.compile(" + JavaLiterals.quote(string(annotation)) + ");");
                    // find semantics like the validator; anchor the regex (^...$) to match the whole value
                    yield "!" + constant + ".matcher(" + name + ").find()";
                }
                case "urlPattern" -> null;
                default -> "";
            };
            if (condition == null) {
                out.append(url(field, name, nullable, indent, imports));
            } else if (!condition.isEmpty()) {
                out.append(indent).append("if (").append(nullable ? name + " != null && " : "").append(condition)
                        .append(") {\n")
                        .append(indent).append("    throw new IllegalArgumentException(")
                        .append(message(field.name(), annotation)).append(");\n")
                        .append(indent).append("}\n");
            }
        }
        return new Checks(out.toString(), List.copyOf(constants));
    }

    private static String compare(final FieldDefinition field, final String name, final Annotation annotation,
                                  final String operator, final Imports imports) {
        final String bound = JavaLiterals.expression(annotation.arguments().getFirst(), field.type(), imports);
        if (field.type() instanceof Type.BasicType basic) {
            switch (basic.builtin().category()) {
                case INTEGER, FLOAT -> {
                    if (!JavaTypes.type(basic, false, imports).equals("BigInteger")) {
                        return name + " " + operator + " " + bound;
                    }
                    return name + ".compareTo(" + bound + ") " + operator + " 0";
                }
                case DECIMAL, DURATION -> {
                    return name + ".compareTo(" + bound + ") " + operator + " 0";
                }
                default -> {
                }
            }
        }
        throw new JavaTypes.UnsupportedTypeException("@@" + annotation.name() + " on " + field.type().text());
    }

    private static String size(final FieldDefinition field, final String name) {
        if (field.type() instanceof Type.BasicType basic) {
            return switch (basic.builtin().category()) {
                case BYTES -> name + ".length";
                case COLLECTION, MAP -> name + ".size()";
                default -> throw new JavaTypes.UnsupportedTypeException("size of " + field.type().text());
            };
        }
        throw new JavaTypes.UnsupportedTypeException("size of " + field.type().text());
    }

    private static String url(final FieldDefinition field, final String name, final boolean nullable,
                              final String indent, final Imports imports) {
        // not a regex: java.net.URI parses RFC 3986 (see the Java guideline)
        final String uri = imports.use("java.net", "URI");
        final String exception = imports.use("java.net", "URISyntaxException");
        final String variable = name + "Uri";
        final String inner = nullable ? indent + "    " : indent;
        final StringBuilder out = new StringBuilder();
        if (nullable) {
            out.append(indent).append("if (").append(name).append(" != null) {\n");
        }
        out.append(inner).append("try {\n")
                .append(inner).append("    final ").append(uri).append(' ').append(variable).append(" = new ")
                .append(uri).append('(').append(name).append(");\n")
                .append(inner).append("    if (!").append(variable).append(".isAbsolute() || ").append(variable)
                .append(".getHost() == null) {\n")
                .append(inner).append("        throw new IllegalArgumentException(\"").append(field.name())
                .append(" must be an absolute URL with a host: \" + ").append(name).append(");\n")
                .append(inner).append("    }\n")
                .append(inner).append("} catch (final ").append(exception).append(" e) {\n")
                .append(inner).append("    throw new IllegalArgumentException(\"").append(field.name())
                .append(" must be a valid URL: \" + ").append(name).append(", e);\n")
                .append(inner).append("}\n");
        if (nullable) {
            out.append(indent).append("}\n");
        }
        return out.toString();
    }

    private static String message(final String field, final Annotation annotation) {
        final String text = switch (annotation.name()) {
            case "min" -> field + " must be at least " + number(annotation);
            case "max" -> field + " must be at most " + number(annotation);
            case "minLength" -> field + " must be at least " + number(annotation) + " characters long";
            case "maxLength" -> field + " must be at most " + number(annotation) + " characters long";
            case "minSize" -> field + " must contain at least " + number(annotation) + " element(s)";
            case "maxSize" -> field + " must contain at most " + number(annotation) + " element(s)";
            default -> field + " must match the pattern " + string(annotation);
        };
        return JavaLiterals.quote(text);
    }

    private static String number(final Annotation annotation) {
        return annotation.arguments().getFirst().text().replace("_", "");
    }

    private static String string(final Annotation annotation) {
        return annotation.arguments().getFirst() instanceof Literal.StringLiteral literal ? literal.value()
                : annotation.arguments().getFirst().text();
    }

    private static String constantName(final String field) {
        return field.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Whether the attribute's type is a {@code bytes} array.
     *
     * @param type the type
     * @return {@code true} for {@code bytes}
     */
    static boolean isBytes(final Type type) {
        return type instanceof Type.BasicType basic && basic.builtin().category() == BuiltinType.Category.BYTES;
    }
}
