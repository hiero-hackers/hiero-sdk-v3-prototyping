package org.hiero.sdk.v3.metalang.generator.java;

import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.generator.GeneratedFile;

/**
 * Generates the {@code @ThreadSafe} annotation of the SDK (see "Thread Safety" in
 * {@code guidelines/api-best-practices-java.md}) and renders its usages. The annotation lives in the package
 * {@value #PACKAGE} of the module that all modules using {@code @@threadSafe} require.
 */
final class ThreadSafeGenerator {

    /** The package of the annotation. */
    static final String PACKAGE = "org.hiero.sdk.annotation";

    /** The simple name of the annotation. */
    static final String NAME = "ThreadSafe";

    private ThreadSafeGenerator() {
    }

    static GeneratedFile generate(final String module) {
        final String java = JavaGenerator.HEADER + "\npackage " + PACKAGE + ";\n\n"
                + """
                import java.lang.annotation.Documented;
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                /// The annotated method or attribute accessor (and setter) may be called concurrently from multiple
                /// threads and is implemented in a thread-safe manner. On a type, it applies to all methods declared by
                /// the type, as if each of them was annotated with the same `group`.
                ///
                /// On an abstract method it is a contract that every implementation must fulfill; on a concrete method
                /// or accessor it states that the implementation is thread-safe.
                ///
                /// Methods and accessors in the same `group` may be called concurrently with each other. Without a
                /// group, an element is only safe for concurrent calls on its own.
                @Documented
                @Retention(RetentionPolicy.CLASS)
                @Target({ElementType.METHOD, ElementType.TYPE})
                public @interface ThreadSafe {

                    /// The group of elements that may be called concurrently with each other; empty for no group.
                    ///
                    /// @return the group name, or an empty string
                    String group() default "";
                }
                """;
        return new GeneratedFile(JavaNames.sourceRoot(module) + "/" + PACKAGE.replace('.', '/') + "/" + NAME
                + ".java", java);
    }

    /**
     * Renders the annotation of an element with {@code @@threadSafe}.
     *
     * @param annotation the {@code @@threadSafe} annotation
     * @param imports    the imports of the file
     * @return {@code @ThreadSafe} or {@code @ThreadSafe(group = "name")}
     */
    static String render(final Annotation annotation, final Imports imports) {
        final String name = imports.use(PACKAGE, NAME);
        return annotation.arguments().isEmpty() ? "@" + name
                : "@" + name + "(group = " + JavaLiterals.quote(annotation.arguments().getFirst().text()) + ")";
    }

    /**
     * Renders the annotation line of a member if it has {@code @@threadSafe} and the owner is not annotated as a
     * whole (then the type-level annotation already covers the member).
     *
     * @param element         the member
     * @param ownerThreadSafe whether the owning Java type carries {@code @ThreadSafe}
     * @param indent          the indentation
     * @param imports         the imports of the file
     * @return the annotation line or an empty string
     */
    static String member(final Annotated element, final boolean ownerThreadSafe, final String indent,
                         final Imports imports) {
        final Optional<Annotation> annotation = element.annotation("threadSafe");
        return annotation.isPresent() && !ownerThreadSafe ? indent + render(annotation.get(), imports) + "\n" : "";
    }
}
