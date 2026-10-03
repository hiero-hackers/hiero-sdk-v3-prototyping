package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * Generates a Java {@code record} for a complex type whose attributes are all {@code @@immutable} (see "Immutable
 * Objects" in {@code guidelines/api-best-practices-java.md}).
 *
 * <ul>
 *   <li>The record components are the effective attributes (inherited ones first); their documentation becomes
 *       {@code @param} tags.</li>
 *   <li>The compact constructor checks non-nullable references with {@code Objects.requireNonNull}, copies
 *       collections ({@code List.copyOf}, ...) and {@code bytes} arrays, and checks the validation annotations
 *       ({@link JavaConstraints}).</li>
 *   <li>Attributes with {@code @@default} get a second constructor without these attributes.</li>
 *   <li>A record with {@code bytes} components returns copies of the arrays, implements {@code equals} and
 *       {@code hashCode} with the content of the arrays (records compare arrays by reference) and prints only the
 *       length of the arrays in {@code toString} (the content may be sensitive, e.g. key material, or large).</li>
 *   <li>Methods are generated with their signature; the body throws {@link UnsupportedOperationException} (see
 *       {@link JavaMembers#method}).</li>
 * </ul>
 */
final class RecordGenerator {

    private static final String INDENT = "    ";

    private RecordGenerator() {
    }

    static GeneratedFile generate(final String module, final TypeDefinition.ComplexTypeDefinition type,
                                  final JavaContext context) {
        final String packageName = JavaNames.packageName(type.name().namespace());
        final String name = type.name().name();
        final Imports imports = context.imports(type);
        final List<FieldDefinition> fields = type.fields();
        // the Java type of every component; accessors that implement an inherited one keep its Java type
        final Map<String, String> declarations = new HashMap<>();
        for (final FieldDefinition field : fields) {
            JavaMembers.accessor(field.name(), false);
            declarations.put(field.name(), JavaTypes.declaration(field.type(), field.hasAnnotation("nullable"),
                    context.boxed(type.name(), field.name()), imports));
        }
        final StringBuilder body = new StringBuilder();
        final List<String> constants = new ArrayList<>();

        // compact constructor
        final StringBuilder checks = new StringBuilder();
        for (final FieldDefinition field : fields) {
            checks.append(normalization(field, declarations.get(field.name()), imports));
            final JavaConstraints.Checks constraints = JavaConstraints.of(field, INDENT + INDENT, imports);
            checks.append(constraints.statements());
            constants.addAll(constraints.constants());
        }
        if (!checks.isEmpty()) {
            final List<String> tags = new ArrayList<>();
            if (checks.indexOf("requireNonNull") >= 0) {
                tags.add("@throws NullPointerException if a required value is `null`");
            }
            if (checks.indexOf("IllegalArgumentException") >= 0) {
                tags.add("@throws IllegalArgumentException if a value violates its constraints");
            }
            body.append(MarkdownComment.render(INDENT, List.of("Creates a new `" + name + "`."), tags));
            body.append(INDENT).append("public ").append(name).append(" {\n").append(checks).append(INDENT)
                    .append("}\n");
        }

        // constructor without the attributes that have a default value
        final List<FieldDefinition> required = fields.stream().filter(f -> !f.hasAnnotation("default")).toList();
        if (required.size() < fields.size()) {
            body.append(body.isEmpty() ? "" : "\n");
            body.append(MarkdownComment.render(INDENT, List.of("Creates a new `" + name + "` with the default value"
                    + " of " + fields.stream().filter(f -> f.hasAnnotation("default"))
                    .map(f -> "`" + f.name() + "`").collect(Collectors.joining(", ")) + ".")));
            body.append(INDENT).append("public ").append(name)
                    .append(parameterList(required.stream().map(f -> "final " + component(f, declarations)).toList(),
                            INDENT)).append(" {\n")
                    .append(INDENT).append(INDENT).append("this(").append(fields.stream()
                            .map(f -> f.hasAnnotation("default")
                                    ? JavaLiterals.expression(f.annotation("default").orElseThrow().arguments()
                                    .getFirst(), f.type(), imports, context)
                                    : JavaKeywords.identifier(f.name()))
                            .collect(Collectors.joining(", "))).append(");\n")
                    .append(INDENT).append("}\n");
        }

        // explicit accessors: copies of bytes arrays; @Deprecated (on a component it only causes a javac warning)
        for (final FieldDefinition field : fields) {
            final boolean bytes = JavaConstraints.isBytes(field.type());
            if (!bytes && !field.hasAnnotation("deprecated")) {
                continue;
            }
            final String component = JavaKeywords.identifier(field.name());
            body.append(body.isEmpty() ? "" : "\n");
            body.append(MarkdownComment.render(INDENT, List.of(field.documentation(), bytes
                    ? "Returns a copy of the `" + field.name() + "` array."
                    : "Returns the `" + field.name() + "` of this value."), List.of(),
                    field.hasAnnotation("deprecated")));
            body.append(INDENT).append("@Override\n");
            if (field.hasAnnotation("deprecated")) {
                body.append(INDENT).append("@Deprecated\n");
            }
            body.append(INDENT).append("public ").append(declarations.get(field.name())).append(' ').append(component)
                    .append("() {\n").append(INDENT).append(INDENT).append("return ")
                    .append(bytes && field.hasAnnotation("nullable") ? component + " == null ? null : " : "")
                    .append(component).append(bytes ? ".clone()" : "").append(";\n").append(INDENT).append("}\n");
        }
        if (fields.stream().anyMatch(f -> JavaConstraints.isBytes(f.type()))) {
            body.append(contentEquality(type, declarations, imports));
        }

        // methods
        for (final MethodDefinition method : type.methods()) {
            body.append(body.isEmpty() ? "" : "\n").append(JavaMembers.method(type.name(), method, context, imports, JavaMembers.Body.STUB));
        }

        // before rendering the imports: the header registers imports too
        final String header = "public record " + name + JavaMembers.typeParameters(type, imports)
                + parameterList(fields.stream().map(f -> component(f, declarations)).toList(), "")
                + context.supertypes(type, "implements", imports);
        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
        java.append("package ").append(packageName).append(";\n\n");
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            java.append(importBlock).append('\n');
        }
        java.append(MarkdownComment.render("", List.of(type.documentation()), parameterTags(fields),
                type.hasAnnotation("deprecated")));
        if (type.hasAnnotation("deprecated")) {
            java.append("@Deprecated\n");
        }
        java.append(header).append(" {\n");
        if (!constants.isEmpty()) {
            java.append('\n');
            constants.forEach(c -> java.append(INDENT).append(c).append('\n'));
        }
        if (!body.isEmpty()) {
            java.append('\n').append(body);
        }
        java.append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, type.name().namespace()) + "/" + name + ".java",
                java.toString());
    }

    /** A parameter list in parentheses; one parameter per line if a single line would be too long. */
    private static String parameterList(final List<String> parameters, final String indent) {
        final String line = "(" + String.join(", ", parameters) + ")";
        if (line.length() <= 80) {
            return line;
        }
        return "(\n" + parameters.stream().map(p -> indent + INDENT + INDENT + p)
                .collect(Collectors.joining(",\n")) + ")";
    }

    private static String component(final FieldDefinition field, final Map<String, String> declarations) {
        return declarations.get(field.name()) + " " + JavaKeywords.identifier(field.name());
    }

    /** Null check and defensive copy of one component ({@code java} is the Java type of the component). */
    private static String normalization(final FieldDefinition field, final String java, final Imports imports) {
        final String name = JavaKeywords.identifier(field.name());
        final boolean nullable = field.hasAnnotation("nullable");
        final String copy = copy(field.type(), name, imports);
        final String indent = INDENT + INDENT;
        if (nullable) {
            return copy == null ? "" : indent + name + " = " + name + " == null ? null : " + copy + ";\n";
        }
        if (JavaTypes.isPrimitive(java)) {
            return "";
        }
        final String check = imports.use("java.util", "Objects") + ".requireNonNull(" + name + ", \"" + field.name()
                + " must not be null\")";
        if (copy == null) {
            return indent + check + ";\n";
        }
        return indent + check + ";\n" + indent + name + " = " + copy + ";\n";
    }

    /** The expression that copies a collection or array, {@code null} for other types. */
    static String copy(final Type type, final String name, final Imports imports) {
        if (type instanceof Type.BasicType basic) {
            final BuiltinType builtin = basic.builtin();
            return switch (builtin.category()) {
                case BYTES -> name + ".clone()";
                case COLLECTION -> imports.use("java.util", builtin.name().equals("set") ? "Set" : "List")
                        + ".copyOf(" + name + ")";
                case MAP -> imports.use("java.util", "Map") + ".copyOf(" + name + ")";
                default -> null;
            };
        }
        return null;
    }

    private static List<String> parameterTags(final List<FieldDefinition> fields) {
        final List<String> tags = new ArrayList<>();
        for (final FieldDefinition field : fields) {
            final List<String> lines = field.documentation().strip().lines().toList();
            if (lines.isEmpty()) {
                continue;
            }
            tags.add("@param " + JavaKeywords.identifier(field.name()) + " " + lines.getFirst());
            lines.subList(1, lines.size()).forEach(l -> tags.add("  " + (l.stripLeading().startsWith("@")
                    ? "&#64;" + l.stripLeading().substring(1) : l)));
        }
        return tags;
    }

    /**
     * {@code equals} and {@code hashCode} that compare arrays by content, and {@code toString} that prints only the
     * length of arrays.
     */
    private static String contentEquality(final TypeDefinition type, final Map<String, String> declarations,
                                          final Imports imports) {
        final String name = type.name().name();
        final List<FieldDefinition> fields = type.fields();
        final String arrays = imports.use("java.util", "Arrays");
        final String objects = imports.use("java.util", "Objects");
        final String nullable = imports.use(JavaTypes.JSPECIFY, "Nullable");
        final String self = name + (type.typeParameters().isEmpty() ? "" : "<?>");
        final StringBuilder out = new StringBuilder();
        if (type.methods("equals").isEmpty()) {
            out.append('\n').append(INDENT).append("@Override\n")
                    .append(INDENT).append("public boolean equals(final @").append(nullable).append(" Object obj) {\n")
                    .append(INDENT).append(INDENT).append("return obj instanceof ").append(self).append(" other")
                    .append(fields.stream().map(f -> "\n" + INDENT + INDENT + INDENT + "&& "
                            + equality(f, declarations.get(f.name()), arrays, objects)).collect(Collectors.joining())).append(";\n")
                    .append(INDENT).append("}\n");
        }
        if (type.methods("hashCode").isEmpty()) {
            out.append('\n').append(INDENT).append("@Override\n")
                    .append(INDENT).append("public int hashCode() {\n")
                    .append(INDENT).append(INDENT).append("return ").append(objects).append(".hash(")
                    .append(fields.stream().map(f -> JavaConstraints.isBytes(f.type())
                            ? arrays + ".hashCode(" + JavaKeywords.identifier(f.name()) + ")"
                            : JavaKeywords.identifier(f.name())).collect(Collectors.joining(", "))).append(");\n")
                    .append(INDENT).append("}\n");
        }
        if (type.methods("toString").isEmpty()) {
            out.append('\n').append(INDENT).append("@Override\n")
                    .append(INDENT).append("public String toString() {\n")
                    .append(INDENT).append(INDENT).append("return \"").append(name).append("[")
                    .append(fields.stream().map(f -> {
                        final String component = JavaKeywords.identifier(f.name());
                        if (!JavaConstraints.isBytes(f.type())) {
                            return f.name() + "=\" + " + component + " + \"";
                        }
                        // only the length: the content may be sensitive (key material) or large (HTTP bodies)
                        final String length = "\"byte[\" + " + component + ".length + \"]\"";
                        return f.name() + "=\" + " + (f.hasAnnotation("nullable")
                                ? "(" + component + " == null ? \"null\" : " + length + ")" : length) + " + \"";
                    }).collect(Collectors.joining(", "))).append("]\";\n")
                    .append(INDENT).append("}\n");
        }
        return out.toString();
    }

    private static String equality(final FieldDefinition field, final String java, final String arrays,
                                   final String objects) {
        final String name = JavaKeywords.identifier(field.name());
        if (JavaConstraints.isBytes(field.type())) {
            return arrays + ".equals(" + name + ", other." + name + ")";
        }
        return switch (java) {
            case "double" -> "Double.compare(" + name + ", other." + name + ") == 0";
            case "byte", "short", "int", "long", "boolean" -> name + " == other." + name;
            default -> objects + ".equals(" + name + ", other." + name + ")";
        };
    }
}
