package org.hiero.sdk.v3.metalang.generator.java;

import java.util.List;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;
import org.hiero.sdk.v3.metalang.model.QualifiedName;

/**
 * Generates the exception class of an error identifier that has no standard JDK exception (see "Custom exception
 * class structure" in {@code guidelines/api-best-practices-java.md}): an unchecked exception with one constructor
 * that takes the message and the (optional) cause.
 */
final class ExceptionGenerator {

    private ExceptionGenerator() {
    }

    static GeneratedFile generate(final String module, final String errorId, final QualifiedName name) {
        final String packageName = JavaNames.packageName(name.namespace());
        final String className = name.name();
        final Imports imports = new Imports(packageName, className);
        final String objects = imports.use("java.util", "Objects");
        final String serial = imports.use("java.io", "Serial");
        final String nullable = imports.use(JavaTypes.JSPECIFY, "Nullable");
        final String body = MarkdownComment.render("", List.of("Signals " + JavaExceptions.anError(errorId) + "."))
                + "public final class " + className + " extends RuntimeException {\n\n"
                + "    @" + serial + "\n"
                + "    private static final long serialVersionUID = 1L;\n\n"
                + MarkdownComment.render("    ", List.of("Creates a new `" + className + "`."), List.of(
                        "@param message the detail message",
                        "@param cause   the cause, or `null` if there is none",
                        "@throws NullPointerException if `message` is `null`"))
                + "    public " + className + "(final String message, final @" + nullable + " Throwable cause) {\n"
                + "        super(" + objects + ".requireNonNull(message, \"message must not be null\"), cause);\n"
                + "    }\n"
                + "}\n";
        return new GeneratedFile(JavaNames.packageDirectory(module, name.namespace()) + "/" + className + ".java",
                JavaGenerator.HEADER + "\npackage " + packageName + ";\n\n" + imports.render() + "\n" + body);
    }
}
