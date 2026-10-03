package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.ConstantDefinition;

/**
 * Generates the constants class of a namespace (see "Constants in Namespaces" in
 * {@code guidelines/api-best-practices-java.md}): a {@code final} class with one {@code public static final} field per
 * constant and a private constructor. Struct literals become constructor calls of the generated record or class.
 */
final class ConstantsGenerator {

    private static final String INDENT = "    ";

    private ConstantsGenerator() {
    }

    /**
     * Returns the name of the constants class of a namespace: the last segment in PascalCase followed by
     * {@code Constants} ({@code consensusnode.transactions} becomes {@code TransactionsConstants}).
     *
     * @param namespace the namespace
     * @return the simple class name
     */
    static String className(final String namespace) {
        final String last = namespace.substring(namespace.lastIndexOf('.') + 1);
        return Character.toUpperCase(last.charAt(0)) + last.substring(1) + "Constants";
    }

    static GeneratedFile generate(final String module, final String namespace,
                                  final List<ConstantDefinition> constants, final JavaContext context) {
        final String packageName = JavaNames.packageName(namespace);
        final String className = className(namespace);
        final Imports imports = context.imports(packageName, className);
        final String fields = constants.stream().map(c -> constant(c, imports, context))
                .collect(Collectors.joining("\n"));
        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
        java.append("package ").append(packageName).append(";\n\n");
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            java.append(importBlock).append('\n');
        }
        java.append(MarkdownComment.render("", List.of("Constants of the package `" + packageName + "`.")))
                .append("public final class ").append(className).append(" {\n\n")
                .append(fields).append('\n')
                .append(INDENT).append("private ").append(className).append("() {\n")
                .append(INDENT).append(INDENT)
                .append("throw new UnsupportedOperationException(\"Constants class cannot be instantiated\");\n")
                .append(INDENT).append("}\n")
                .append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, namespace) + "/" + className + ".java",
                java.toString());
    }

    /**
     * Renders one constant.
     *
     * @param constant the constant
     * @param imports  the imports of the file
     * @param context  the generation context
     * @return the field declaration with its documentation
     * @throws JavaTypes.UnsupportedTypeException if the type or the value has no Java form
     */
    static String constant(final ConstantDefinition constant, final Imports imports, final JavaContext context) {
        final String declaration = JavaTypes.declaration(constant.type(), constant.hasAnnotation("nullable"),
                imports);
        final String value = JavaLiterals.expression(constant.value(), constant.type(), imports, context);
        return MarkdownComment.render(INDENT, List.of(constant.documentation()), List.of(),
                constant.hasAnnotation("deprecated"))
                + (constant.hasAnnotation("deprecated") ? INDENT + "@Deprecated\n" : "")
                + INDENT + "public static final " + declaration + " " + constant.name().name() + " = " + value
                + ";\n";
    }
}
