package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.ParameterDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;

/**
 * Generates the {@code @FunctionalInterface} of a function type that no {@code java.util.function} interface matches
 * (three or more parameters, varargs), see "Function Types" in {@code guidelines/api-best-practices-java.md}. The
 * interface is named after the function ({@code function<bool onMessage(...)>} becomes {@code OnMessageFunction}) and
 * declares the function as its single abstract method.
 */
final class FunctionInterfaceGenerator {

    private FunctionInterfaceGenerator() {
    }

    /**
     * Returns the name of the interface of a function type: the function name in PascalCase followed by
     * {@code Function}.
     *
     * @param function the function type
     * @return the simple interface name
     */
    static String className(final Type.FunctionType function) {
        return Character.toUpperCase(function.name().charAt(0)) + function.name().substring(1) + "Function";
    }

    static GeneratedFile generate(final String module, final QualifiedName name, final Type.FunctionType function,
                                  final JavaContext context) {
        final String packageName = JavaNames.packageName(name.namespace());
        final Imports imports = context.imports(packageName, name.name());
        final String method = signature(function, imports);
        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
        java.append("package ").append(packageName).append(";\n\n");
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            java.append(importBlock).append('\n');
        }
        java.append(MarkdownComment.render("", List.of("A function `" + function.name() + "` with the parameters "
                        + function.parameters().stream().map(p -> "`" + p.name() + "`")
                        .collect(Collectors.joining(", ")) + ".")))
                .append("@FunctionalInterface\n")
                .append("public interface ").append(name.name()).append(" {\n\n")
                .append("    ").append(method).append(";\n")
                .append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, name.namespace()) + "/" + name.name() + ".java",
                java.toString());
    }

    /**
     * Renders the single abstract method of the interface.
     *
     * @param function the function type
     * @param imports  the imports of the file
     * @return the method signature without semicolon
     * @throws JavaTypes.UnsupportedTypeException if a type has no Java form
     */
    static String signature(final Type.FunctionType function, final Imports imports) {
        return JavaTypes.declaration(function.returnType(), false, imports) + " "
                + JavaKeywords.identifier(function.name()) + "(" + function.parameters().stream()
                .map(p -> parameter(p, imports)).collect(Collectors.joining(", ")) + ")";
    }

    private static String parameter(final ParameterDefinition parameter, final Imports imports) {
        final String name = JavaKeywords.identifier(parameter.name());
        if (parameter.varargs()) {
            return JavaTypes.declaration(parameter.type(), false, imports) + "... " + name;
        }
        return JavaTypes.declaration(parameter.type(), parameter.hasAnnotation("nullable"), imports) + " " + name;
    }
}
