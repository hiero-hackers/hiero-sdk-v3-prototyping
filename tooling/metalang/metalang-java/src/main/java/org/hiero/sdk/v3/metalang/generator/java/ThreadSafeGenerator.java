package org.hiero.sdk.v3.metalang.generator.java;

import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;

/**
 * Renders the usages of the {@code @ThreadSafe} annotation of the SDK (see "Thread Safety" in
 * {@code guidelines/api-best-practices-java.md}). The annotation lives in the package {@value #PACKAGE} of the
 * hand-written support module ({@code sdk-java/support}).
 */
final class ThreadSafeGenerator {

    /** The package of the annotation. */
    static final String PACKAGE = "org.hiero.sdk.annotation";

    /** The simple name of the annotation. */
    static final String NAME = "ThreadSafe";

    private ThreadSafeGenerator() {
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
