package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.FieldDefinition;
import org.hiero.sdk.v3.metalang.model.MethodDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;

/**
 * Generates a Java {@code interface} for an abstraction (see "Complex Types" in
 * {@code guidelines/api-best-practices-java.md}).
 *
 * <ul>
 *   <li>Every attribute declared by the abstraction becomes an abstract accessor {@code name()}; a mutable attribute
 *       (without {@code @@immutable}) also gets {@code setName(value)}, which returns the self type (a type parameter
 *       {@code $$Self extends Name<..., $$Self>}) or the interface itself, so that calls can be chained.</li>
 *   <li>Declared methods are abstract; {@code @@static} methods get a body that throws
 *       {@link UnsupportedOperationException}.</li>
 *   <li>Supertypes become {@code extends}; {@code @@sealed(A, B)} becomes {@code sealed ... permits A, B}, and an
 *       interface that extends a sealed interface is {@code non-sealed}.</li>
 * </ul>
 */
final class InterfaceGenerator {

    private static final String INDENT = "    ";

    private InterfaceGenerator() {
    }

    static GeneratedFile generate(final String module, final TypeDefinition.ComplexTypeDefinition type,
                                  final JavaContext context) {
        final String packageName = JavaNames.packageName(type.name().namespace());
        final String name = type.name().name();
        final Imports imports = context.imports(type);
        final StringBuilder body = new StringBuilder();

        for (final FieldDefinition field : type.declaredFields()) {
            final String accessor = JavaMembers.accessor(field.name(), false);
            final String declaration = JavaTypes.declaration(field.type(), field.hasAnnotation("nullable"),
                    context.boxed(type.name(), field.name()), imports);
            body.append(body.isEmpty() ? "" : "\n");
            body.append(MarkdownComment.render(INDENT, List.of(field.documentation().isBlank()
                    ? "Returns the `" + field.name() + "`." : field.documentation()), List.of(),
                    field.hasAnnotation("deprecated")));
            if (context.overridesAccessor(type.name(), field.name())) {
                body.append(INDENT).append("@Override\n");
            }
            if (field.hasAnnotation("deprecated")) {
                body.append(INDENT).append("@Deprecated\n");
            }
            body.append(INDENT).append(declaration).append(' ').append(accessor).append("();\n");
            if (!field.hasAnnotation("immutable")) {
                body.append('\n');
                body.append(MarkdownComment.render(INDENT, List.of("Sets the `" + field.name() + "`.",
                                MarkdownComment.deprecationReason(field.documentation())),
                        List.of("@param " + accessor + " the new value", "@return this object"),
                        field.hasAnnotation("deprecated")));
                if (field.hasAnnotation("deprecated")) {
                    body.append(INDENT).append("@Deprecated\n");
                }
                body.append(INDENT).append(JavaMembers.selfType(type, imports)).append(' ').append(setter(field.name()))
                        .append("(final ").append(declaration).append(' ').append(accessor).append(");\n");
            }
        }
        for (final MethodDefinition method : type.declaredMethods()) {
            body.append(body.isEmpty() ? "" : "\n").append(JavaMembers.method(type.name(), method, context, imports, JavaMembers.Body.INTERFACE));
        }

        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
        java.append("package ").append(packageName).append(";\n\n");
        final String header = header(type, context, imports);
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            java.append(importBlock).append('\n');
        }
        java.append(MarkdownComment.render("", List.of(type.documentation()), List.of(),
                type.hasAnnotation("deprecated")));
        if (type.hasAnnotation("deprecated")) {
            java.append("@Deprecated\n");
        }
        java.append(header).append(" {\n");
        if (!body.isEmpty()) {
            java.append('\n').append(body);
        }
        java.append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, type.name().namespace()) + "/" + name + ".java",
                java.toString());
    }

    private static String header(final TypeDefinition.ComplexTypeDefinition type, final JavaContext context,
                                 final Imports imports) {
        final List<QualifiedName> permitted = permittedSubtypes(type);
        final StringBuilder java = new StringBuilder("public ");
        if (!permitted.isEmpty()) {
            java.append("sealed ");
        } else if (type.supertypes().stream().anyMatch(s -> s instanceof Type.DeclaredType d
                && context.isGenerated(d.name()) && context.model().type(d.name())
                .map(t -> !permittedSubtypes(t).isEmpty()).orElse(false))) {
            java.append("non-sealed ");
        }
        java.append("interface ").append(type.name().name()).append(JavaMembers.typeParameters(type, imports))
                .append(context.supertypes(type, "extends", imports));
        if (!permitted.isEmpty()) {
            java.append(" permits ").append(permitted.stream()
                    .map(p -> imports.use(JavaNames.packageName(p.namespace()), p.name()))
                    .collect(Collectors.joining(", ")));
        }
        return java.toString();
    }

    /**
     * Returns the types listed in {@code @@sealed(...)}: simple names are in the namespace of the type,
     * {@code ns.Type} names are qualified.
     *
     * @param type the type
     * @return the permitted subtypes, empty if the type is not sealed
     */
    static List<QualifiedName> permittedSubtypes(final TypeDefinition type) {
        final List<QualifiedName> result = new ArrayList<>();
        type.annotation("sealed").ifPresent(sealed -> {
            for (final Literal argument : sealed.arguments()) {
                final String text = argument.text();
                final int dot = text.lastIndexOf('.');
                result.add(dot < 0 ? new QualifiedName(type.name().namespace(), text)
                        : new QualifiedName(text.substring(0, dot), text.substring(dot + 1)));
            }
        });
        return result;
    }

    static String setter(final String attribute) {
        return "set" + Character.toUpperCase(attribute.charAt(0)) + attribute.substring(1);
    }
}
