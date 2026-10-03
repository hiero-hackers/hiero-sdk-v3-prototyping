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
 *       of the attribute list, with a getter each ({@code getSymbol()}). Non-nullable reference attributes are
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

    static GeneratedFile generate(final String module, final TypeDefinition.EnumDefinition enumType) {
        final String packageName = JavaNames.packageName(enumType.name().namespace());
        final String name = enumType.name().name();
        final Imports imports = new Imports(packageName, name);
        final StringBuilder body = new StringBuilder();

        // values
        final List<EnumValueDefinition> values = enumType.values();
        for (int i = 0; i < values.size(); i++) {
            final EnumValueDefinition value = values.get(i);
            body.append(MarkdownComment.render("    ", List.of(value.documentation())));
            if (value.hasAnnotation("deprecated")) {
                body.append("    @Deprecated\n");
            }
            body.append("    ").append(value.name());
            if (!value.arguments().isEmpty()) {
                final List<String> arguments = new ArrayList<>();
                for (int a = 0; a < value.arguments().size(); a++) {
                    final Literal argument = value.arguments().get(a);
                    arguments.add(JavaLiterals.expression(argument, enumType.attributes().get(a).type(), imports));
                }
                body.append('(').append(String.join(", ", arguments)).append(')');
            }
            body.append(i + 1 < values.size() ? ",\n" : ";\n");
        }
        if (values.isEmpty()) {
            body.append("    ;\n");
        }

        // fields, constructor, getters
        if (!enumType.attributes().isEmpty()) {
            body.append('\n');
            for (final ParameterDefinition attribute : enumType.attributes()) {
                body.append("    private final ").append(JavaMembers.declaration(attribute.type(), attribute, imports)).append(' ')
                        .append(JavaKeywords.identifier(attribute.name())).append(";\n");
            }
            body.append('\n');
            body.append("    ").append(name).append('(').append(enumType.attributes().stream()
                    .map(a -> "final " + JavaMembers.declaration(a.type(), a, imports) + " " + JavaKeywords.identifier(a.name()))
                    .collect(Collectors.joining(", "))).append(") {\n");
            for (final ParameterDefinition attribute : enumType.attributes()) {
                final String field = JavaKeywords.identifier(attribute.name());
                final boolean check = !attribute.hasAnnotation("nullable")
                        && !JavaTypes.isPrimitive(JavaTypes.type(attribute.type(), false, imports));
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
                body.append("    public ").append(JavaMembers.declaration(attribute.type(), attribute, imports)).append(' ')
                        .append(JavaKeywords.getter(attribute.name())).append("() {\n")
                        .append("        return ").append(field)
                        .append(JavaTypes.type(attribute.type(), false, imports).endsWith("[]") ? ".clone()" : "")
                        .append(";\n    }\n");
            }
        }

        // methods
        for (final MethodDefinition method : enumType.methods()) {
            body.append('\n').append(JavaMembers.method(name, method, imports));
        }

        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
        java.append("package ").append(packageName).append(";\n\n");
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            java.append(importBlock).append('\n');
        }
        java.append(MarkdownComment.render("", List.of(enumType.documentation())));
        if (enumType.hasAnnotation("deprecated")) {
            java.append("@Deprecated\n");
        }
        java.append("public enum ").append(name);
        java.append(JavaMembers.implementsComment(enumType.supertypes()));
        java.append(" {\n\n").append(body).append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, enumType.name().namespace()) + "/" + name
                + ".java", java.toString());
    }
}
