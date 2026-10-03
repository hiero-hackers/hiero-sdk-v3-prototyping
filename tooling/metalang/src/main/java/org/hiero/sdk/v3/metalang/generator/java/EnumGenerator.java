package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * Generates a Java {@code enum} (see "Enumerations" in {@code guidelines/api-best-practices-java.md}).
 *
 * <ul>
 *   <li>Attributes of the attribute list become {@code private final} fields, set by the constructor in the order
 *       of the attribute list, with an accessor each ({@code symbol()}). Non-nullable reference attributes are
 *       checked with {@code Objects.requireNonNull}.</li>
 *   <li>Methods are generated with their signature; their behaviour is only described in the spec, so the body
 *       throws {@link UnsupportedOperationException} until an implementation strategy exists.</li>
 *   <li>Supertypes (abstractions) are not generated yet, so the {@code implements} clause is written as a comment.
 *   </li>
 * </ul>
 */
final class EnumGenerator {

    private EnumGenerator() {
    }

    static GeneratedFile generate(final String module, final TypeDefinition.EnumDefinition enumType,
                                  final JavaContext context) {
        final String packageName = JavaNames.packageName(enumType.name().namespace());
        final String name = enumType.name().name();
        final Imports imports = context.imports(enumType);
        final StringBuilder body = new StringBuilder();

        // values
        final List<EnumValueDefinition> values = enumType.values();
        for (int i = 0; i < values.size(); i++) {
            final EnumValueDefinition value = values.get(i);
            body.append(MarkdownComment.render("    ", List.of(value.documentation()), List.of(),
                    value.hasAnnotation("deprecated")));
            if (value.hasAnnotation("deprecated")) {
                body.append("    @Deprecated\n");
            }
            body.append("    ").append(value.name());
            if (!value.arguments().isEmpty()) {
                final List<String> arguments = new ArrayList<>();
                for (int a = 0; a < value.arguments().size(); a++) {
                    final Literal argument = value.arguments().get(a);
                    arguments.add(JavaLiterals.expression(argument, enumType.attributes().get(a).type(), imports,
                            context));
                }
                body.append('(').append(String.join(", ", arguments)).append(')');
            }
            body.append(i + 1 < values.size() ? ",\n" : ";\n");
        }
        if (values.isEmpty()) {
            body.append("    ;\n");
        }

        // fields, constructor, accessors
        if (!enumType.attributes().isEmpty()) {
            body.append('\n');
            for (final ParameterDefinition attribute : enumType.attributes()) {
                body.append("    private final ").append(declaration(enumType, attribute, context, imports))
                        .append(' ').append(JavaKeywords.identifier(attribute.name())).append(";\n");
            }
            body.append('\n');
            body.append("    ").append(name).append('(').append(enumType.attributes().stream()
                    .map(a -> "final " + declaration(enumType, a, context, imports) + " "
                            + JavaKeywords.identifier(a.name()))
                    .collect(Collectors.joining(", "))).append(") {\n");
            for (final ParameterDefinition attribute : enumType.attributes()) {
                final String field = JavaKeywords.identifier(attribute.name());
                final boolean check = !attribute.hasAnnotation("nullable")
                        && !JavaTypes.isPrimitive(JavaTypes.type(attribute.type(), false, imports))
                        || context.boxed(enumType.name(), attribute.name()) && !attribute.hasAnnotation("nullable");
                body.append("        this.").append(field).append(" = ").append(check
                        ? imports.use("java.util", "Objects") + ".requireNonNull(" + field + ", \"" + attribute.name()
                        + " must not be null\")" : field).append(";\n");
            }
            body.append("    }\n");
            for (final ParameterDefinition attribute : enumType.attributes()) {
                body.append('\n');
                body.append(MarkdownComment.render("    ", List.of("Returns the `" + attribute.name()
                        + "` of this value.")));
                final String field = JavaKeywords.identifier(attribute.name());
                if (context.overridesAccessor(enumType.name(), attribute.name())) {
                    body.append("    @Override\n");
                }
                body.append("    public ").append(declaration(enumType, attribute, context, imports)).append(' ')
                        .append(JavaMembers.accessor(attribute.name(), true)).append("() {\n")
                        .append("        return ").append(field)
                        .append(JavaTypes.type(attribute.type(), false, imports).endsWith("[]") ? ".clone()" : "")
                        .append(";\n    }\n");
            }
        }

        // methods
        for (final MethodDefinition method : enumType.methods()) {
            body.append('\n').append(JavaMembers.method(enumType.name(), method, context, imports, JavaMembers.Body.STUB));
        }

        // before rendering the imports: the clause registers imports too
        final String supertypes = context.supertypes(enumType, "implements", imports);
        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
        java.append("package ").append(packageName).append(";\n\n");
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            java.append(importBlock).append('\n');
        }
        java.append(MarkdownComment.render("", List.of(enumType.documentation()), List.of(),
                enumType.hasAnnotation("deprecated")));
        if (enumType.hasAnnotation("deprecated")) {
            java.append("@Deprecated\n");
        }
        java.append("public enum ").append(name);
        java.append(supertypes);
        java.append(" {\n\n").append(body).append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, enumType.name().namespace()) + "/" + name
                + ".java", java.toString());
    }

    private static String declaration(final TypeDefinition.EnumDefinition enumType,
                                      final ParameterDefinition attribute, final JavaContext context,
                                      final Imports imports) {
        return JavaTypes.declaration(attribute.type(), attribute.hasAnnotation("nullable"),
                context.boxed(enumType.name(), attribute.name()), imports);
    }
}
