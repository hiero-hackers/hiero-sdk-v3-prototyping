package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.EnumValueDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;

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
                body.append("    private final ").append(declaration(attribute.type(), attribute, imports)).append(' ')
                        .append(JavaKeywords.identifier(attribute.name())).append(";\n");
            }
            body.append('\n');
            body.append("    ").append(name).append('(').append(enumType.attributes().stream()
                    .map(a -> "final " + declaration(a.type(), a, imports) + " " + JavaKeywords.identifier(a.name()))
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
                body.append("    public ").append(declaration(attribute.type(), attribute, imports)).append(' ')
                        .append(JavaKeywords.getter(attribute.name())).append("() {\n")
                        .append("        return ").append(field)
                        .append(JavaTypes.type(attribute.type(), false, imports).endsWith("[]") ? ".clone()" : "")
                        .append(";\n    }\n");
            }
        }

        // methods
        for (final MethodDefinition method : enumType.methods()) {
            body.append('\n').append(method(name, method, imports));
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
        if (!enumType.supertypes().isEmpty()) {
            java.append(" /* implements ").append(enumType.supertypes().stream()
                    .map(t -> t instanceof Type.DeclaredType d
                            ? JavaNames.packageName(d.name().namespace()) + "." + d.name().name() : t.text())
                    .collect(Collectors.joining(", "))).append(" (enabled as soon as abstractions are generated) */");
        }
        java.append(" {\n\n").append(body).append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, enumType.name().namespace()) + "/" + name
                + ".java", java.toString());
    }

    /**
     * Renders a method whose behaviour is not generated: signature, Javadoc and a body that throws
     * {@link UnsupportedOperationException}.
     */
    static String method(final String owner, final MethodDefinition method, final Imports imports) {
        final StringBuilder java = new StringBuilder();
        final List<String> tags = method.annotation("throws").stream()
                .flatMap(t -> t.arguments().stream())
                .map(a -> JavaExceptions.uncheckedStandardException(a.text()))
                .flatMap(Optional::stream)
                .distinct()
                .map(e -> "@throws " + e)
                .toList();
        java.append(MarkdownComment.render("    ", List.of(method.documentation()), tags));
        if (method.hasAnnotation("deprecated")) {
            java.append("    @Deprecated\n");
        }
        java.append("    public ");
        if (method.isStatic()) {
            java.append("static ");
        }
        if (!method.typeParameters().isEmpty()) {
            java.append('<').append(method.typeParameters().stream().map(p -> typeParameter(p, imports))
                    .collect(Collectors.joining(", "))).append("> ");
        }
        java.append(returnType(method, imports)).append(' ').append(JavaKeywords.identifier(method.name())).append('(')
                .append(method.parameters().stream().map(p -> parameter(p, imports)).collect(Collectors.joining(", ")))
                .append(") {\n")
                .append("        throw new UnsupportedOperationException(\"Not implemented yet: ").append(owner)
                .append('.').append(method.name()).append("\");\n")
                .append("    }\n");
        return java.toString();
    }

    private static String returnType(final MethodDefinition method, final Imports imports) {
        if (method.hasAnnotation("streaming")) {
            throw new JavaTypes.UnsupportedTypeException("@@streaming " + method.returnType().text());
        }
        if (method.hasAnnotation("async")) {
            final String result = JavaTypes.type(method.returnType(), true, imports);
            return JavaTypes.annotate(imports.use("java.util.concurrent", "CompletionStage") + "<" + result + ">",
                    imports.use(JavaTypes.JSPECIFY, "NonNull"));
        }
        return JavaTypes.declaration(method.returnType(), method.hasAnnotation("nullable"), imports);
    }

    private static String parameter(final ParameterDefinition parameter, final Imports imports) {
        final String name = JavaKeywords.identifier(parameter.name());
        if (parameter.varargs()) {
            return "final " + JavaTypes.declaration(parameter.type(), false, imports) + "... " + name;
        }
        return "final " + declaration(parameter.type(), parameter, imports) + " " + name;
    }

    private static String typeParameter(final TypeParameterDefinition parameter, final Imports imports) {
        final String name = parameter.name().substring(2);
        return parameter.bound() == null ? name : name + " extends " + JavaTypes.type(parameter.bound(), true, imports);
    }

    private static String declaration(final Type type, final ParameterDefinition element, final Imports imports) {
        return JavaTypes.declaration(type, element.hasAnnotation("nullable"), imports);
    }

}
