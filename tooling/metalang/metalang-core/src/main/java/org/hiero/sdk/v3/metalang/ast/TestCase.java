package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A test of a spec's {@code tests} block: {@code test "name" { … }} (see {@code guidelines/test-language.md}).
 *
 * @param name          the name, which describes the tested behaviour
 * @param annotations   the annotations, e.g. {@code @@disabled("reason")}
 * @param body          the statements
 * @param documentation the comment above the test
 * @param location      the location of the {@code test} keyword
 */
public record TestCase(String name, List<Annotation> annotations, List<Statement> body, String documentation,
                       SourceLocation location) implements Node {

    /**
     * Creates a test.
     *
     * @param name          the name
     * @param annotations   the annotations
     * @param body          the statements
     * @param documentation the documentation
     * @param location      the location
     */
    public TestCase {
        Objects.requireNonNull(name, "name must not be null");
        annotations = List.copyOf(annotations);
        body = List.copyOf(body);
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
