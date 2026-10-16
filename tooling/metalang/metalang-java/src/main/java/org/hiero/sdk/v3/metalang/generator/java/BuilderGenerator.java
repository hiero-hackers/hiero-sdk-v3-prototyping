package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * Generates the builder of a record or class (see "Builder Pattern" in {@code guidelines/api-best-practices-java.md}):
 * a static method {@code builder()} and a nested class {@code Builder} with one method per constructor parameter,
 * named after the attribute, and {@code build()}, which calls the constructor - so the checks of the constructor are
 * the checks of the builder. Attributes that are not set are {@code null}, or have their {@code @@default} value.
 *
 * <p>A type gets a builder if it is concrete and its constructor takes more than {@value #MAX_PARAMETERS} values or at
 * least {@value #MIN_OPTIONAL} optional ones ({@code @@nullable} or {@code @@default}): many or optional parameters
 * make a constructor call unreadable. It gets none if another type extends it - the static {@code builder()} of a
 * subclass could not hide the one of its superclass, their builders are unrelated types - or if a name of the builder
 * would clash (an attribute named {@code build}, a member named {@code builder}, a type named {@code Builder}).
 */
final class BuilderGenerator {

    /** The number of constructor parameters above which a type gets a builder. */
    static final int MAX_PARAMETERS = 4;

    /** The number of optional constructor parameters from which on a type gets a builder. */
    static final int MIN_OPTIONAL = 2;

    private static final String INDENT = "    ";

    private BuilderGenerator() {
    }

    /**
     * Whether a type gets a builder.
     *
     * @param type       the type
     * @param parameters the parameters of its constructor
     * @param context    the generation context
     * @return whether it gets a builder
     */
    static boolean hasBuilder(final TypeDefinition.ComplexTypeDefinition type, final List<FieldDefinition> parameters,
                              final JavaContext context) {
        final boolean extended = context.model().types().stream().anyMatch(t -> t.supertypes().stream()
                .anyMatch(s -> s instanceof org.hiero.sdk.v3.metalang.model.Type.DeclaredType d
                        && d.name().equals(type.name())));
        if (type.abstraction() || extended || parameters.stream().anyMatch(f -> f.name().equals("build"))
                || type.fields().stream().anyMatch(f -> f.name().equals("builder"))
                || !type.methods("builder").isEmpty() || !type.methods("build").isEmpty()
                || type.name().name().equals("Builder")) {
            return false;
        }
        final long optional = parameters.stream().filter(BuilderGenerator::optional).count();
        return parameters.size() > MAX_PARAMETERS || optional >= MIN_OPTIONAL;
    }

    /** Whether a constructor parameter may be left out of the builder. */
    static boolean optional(final FieldDefinition field) {
        return field.hasAnnotation("nullable") || field.hasAnnotation("default");
    }

    /**
     * Renders {@code builder()} and the class {@code Builder}.
     *
     * @param type         the type
     * @param parameters   the parameters of its constructor, in order
     * @param declarations the Java type of every attribute
     * @param imports      the imports of the file
     * @param context      the generation context
     * @param validated    whether the constructor checks the validation annotations
     * @return the members, separated by blank lines
     */
    static List<String> render(final TypeDefinition.ComplexTypeDefinition type, final List<FieldDefinition> parameters,
                               final Map<String, String> declarations, final Imports imports,
                               final JavaContext context, final boolean validated) {
        final String name = type.name().name();
        final String typeParameters = JavaMembers.typeParameters(type, imports);
        final String arguments = type.typeParameters().isEmpty() ? "" : "<" + type.typeParameters().stream()
                .map(p -> imports.typeVariable(p.name())).collect(Collectors.joining(", ")) + ">";
        final String builder = "Builder" + arguments;
        final List<String> members = new ArrayList<>();
        members.add(MarkdownComment.render(INDENT, List.of("Returns a builder for a new `" + name + "`."),
                List.of("@return the builder")) + INDENT + "public static " + (typeParameters.isEmpty() ? ""
                : typeParameters + " ") + builder + " builder() {\n" + INDENT + INDENT + "return new Builder"
                + (arguments.isEmpty() ? "" : "<>") + "();\n" + INDENT + "}\n");

        final StringBuilder java = new StringBuilder();
        java.append(MarkdownComment.render(INDENT, List.of("The builder of `" + name + "`.", "`build()` creates the `"
                + name + "` of the values set one by one.", "A value that is not set is `null`, or has its default "
                + "value.")));
        java.append(INDENT).append("public static final class Builder").append(typeParameters).append(" {\n");
        // the state
        java.append('\n');
        for (final FieldDefinition field : parameters) {
            final boolean nonNull = field.hasAnnotation("default") && !field.hasAnnotation("nullable");
            final String declaration = nonNull ? declarations.get(field.name())
                    : JavaTypes.declaration(field.type(), true, imports);
            java.append(INDENT).append(INDENT).append("private ").append(declaration).append(' ')
                    .append(JavaKeywords.identifier(field.name()));
            if (field.hasAnnotation("default")) {
                java.append(" = ").append(JavaLiterals.expression(field.annotation("default").orElseThrow()
                        .arguments().getFirst(), field.type(), imports, context));
            }
            java.append(";\n");
        }
        java.append('\n').append(INDENT).append(INDENT).append("private Builder() {\n").append(INDENT).append(INDENT)
                .append(INDENT).append("// created by ").append(name).append(".builder()\n").append(INDENT)
                .append(INDENT).append("}\n");
        // one method per value
        for (final FieldDefinition field : parameters) {
            final String identifier = JavaKeywords.identifier(field.name());
            java.append('\n').append(MarkdownComment.render(INDENT + INDENT, List.of("Sets the `" + field.name()
                            + "`.", field.documentation(), MarkdownComment.deprecationReason(field.documentation())),
                    List.of("@param " + identifier + " the value", "@return this builder"),
                    field.hasAnnotation("deprecated")));
            if (field.hasAnnotation("deprecated")) {
                java.append(INDENT).append(INDENT).append("@Deprecated\n");
            }
            java.append(INDENT).append(INDENT).append("public ").append(builder).append(' ').append(identifier)
                    .append("(final ").append(declarations.get(field.name())).append(' ').append(identifier)
                    .append(") {\n")
                    .append(INDENT).append(INDENT).append(INDENT).append("this.").append(identifier).append(" = ")
                    .append(identifier).append(";\n")
                    .append(INDENT).append(INDENT).append(INDENT).append("return this;\n")
                    .append(INDENT).append(INDENT).append("}\n");
        }
        // build
        final List<String> values = new ArrayList<>();
        boolean required = false;
        for (final FieldDefinition field : parameters) {
            final String identifier = JavaKeywords.identifier(field.name());
            if (optional(field)) {
                values.add(identifier);
            } else {
                required = true;
                values.add(imports.use("java.util", "Objects") + ".requireNonNull(" + identifier + ", \""
                        + field.name() + " must not be null\")");
            }
        }
        final List<String> tags = new ArrayList<>();
        tags.add("@return the new `" + name + "`");
        if (required) {
            tags.add("@throws NullPointerException if a required value is not set");
        }
        if (validated) {
            tags.add("@throws IllegalArgumentException if a value violates its constraints");
        }
        java.append('\n').append(MarkdownComment.render(INDENT + INDENT, List.of("Creates the `" + name
                + "` of the values set."), tags));
        java.append(INDENT).append(INDENT).append("public ").append(name).append(arguments).append(" build() {\n")
                .append(INDENT).append(INDENT).append(INDENT).append("return new ").append(name)
                .append(arguments.isEmpty() ? "" : "<>").append('(');
        final String line = String.join(", ", values);
        if (line.length() + 36 + name.length() <= 120) {
            java.append(line);
        } else {
            java.append('\n').append(values.stream().map(v -> INDENT.repeat(5) + v)
                    .collect(Collectors.joining(",\n")));
        }
        java.append(");\n").append(INDENT).append(INDENT).append("}\n").append(INDENT).append("}\n");
        members.add(java.toString());
        return members;
    }
}
