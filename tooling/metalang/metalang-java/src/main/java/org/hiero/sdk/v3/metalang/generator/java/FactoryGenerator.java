package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import java.util.stream.Collectors;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.FunctionDefinition;
import org.hiero.sdk.v3.metalang.model.QualifiedName;

/**
 * Generates the factory class of a namespace: the namespace-level functions (always {@code @@static}) become
 * {@code public static} methods of a {@code final} class with a private constructor, analogous to the constants class
 * ({@link ConstantsGenerator}). The behaviour is not generated, so the bodies throw
 * {@link UnsupportedOperationException}.
 */
final class FactoryGenerator {

    private static final String INDENT = "    ";

    private FactoryGenerator() {
    }

    /**
     * Returns the name of the factory class of a namespace: the last segment in PascalCase followed by
     * {@code Factory} ({@code mirrornode.account} becomes {@code AccountFactory}).
     *
     * @param namespace the namespace
     * @return the simple class name
     */
    static String className(final String namespace) {
        final String last = namespace.substring(namespace.lastIndexOf('.') + 1);
        return Character.toUpperCase(last.charAt(0)) + last.substring(1) + "Factory";
    }

    static GeneratedFile generate(final String module, final String namespace,
                                  final List<FunctionDefinition> functions, final JavaContext context) {
        final String packageName = JavaNames.packageName(namespace);
        final String className = className(namespace);
        final Imports imports = context.imports(packageName, className);
        final String methods = functions.stream().map(f -> function(f, imports, context))
                .collect(Collectors.joining("\n"));
        final StringBuilder java = new StringBuilder(JavaGenerator.HEADER).append('\n');
        java.append("package ").append(packageName).append(";\n\n");
        final String importBlock = imports.render();
        if (!importBlock.isEmpty()) {
            java.append(importBlock).append('\n');
        }
        java.append(MarkdownComment.render("", List.of("Factory methods of the package `" + packageName + "`.")))
                .append("public final class ").append(className).append(" {\n\n")
                .append(methods).append('\n')
                .append(INDENT).append("private ").append(className).append("() {\n")
                .append(INDENT).append(INDENT)
                .append("throw new UnsupportedOperationException(\"Factory class cannot be instantiated\");\n")
                .append(INDENT).append("}\n")
                .append("}\n");
        return new GeneratedFile(JavaNames.packageDirectory(module, namespace) + "/" + className + ".java",
                java.toString());
    }

    /**
     * Renders one function as static method.
     *
     * @param function the function
     * @param imports  the imports of the file
     * @param context  the generation context
     * @return the method with its documentation
     * @throws JavaTypes.UnsupportedTypeException if a type of the function has no Java form
     */
    static String function(final FunctionDefinition function, final Imports imports, final JavaContext context) {
        return JavaMembers.method(new QualifiedName(function.namespace(), className(function.namespace())),
                function.method(), context, imports, JavaMembers.Body.STUB);
    }
}
