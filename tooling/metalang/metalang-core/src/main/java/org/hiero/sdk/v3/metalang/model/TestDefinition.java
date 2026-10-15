package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * A resolved test of a spec (see {@code guidelines/test-language.md}): every expression is resolved against the
 * model and knows its type.
 *
 * @param id            the id, {@code <namespace>/<name in kebab-case>}
 * @param name          the name, which describes the tested behaviour
 * @param namespace     the namespace of the spec
 * @param specName      the name of the spec file without directory and extension, e.g. {@code native-token}
 * @param annotations   the annotations of the test
 * @param body          the statements
 * @param documentation the comment above the test
 * @param location      the location of the test
 */
public record TestDefinition(String id, String name, String namespace, String specName, List<Annotation> annotations,
                             List<Statement> body, String documentation, SourceLocation location) {

    /**
     * Creates a test.
     *
     * @param id            the id
     * @param name          the name
     * @param namespace     the namespace
     * @param specName      the spec file name
     * @param annotations   the annotations
     * @param body          the statements
     * @param documentation the documentation
     * @param location      the location
     */
    public TestDefinition {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(specName, "specName must not be null");
        annotations = List.copyOf(annotations);
        body = List.copyOf(body);
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Returns the annotation with the given name.
     *
     * @param annotationName the name without {@code @@}
     * @return the annotation, if present
     */
    public Optional<Annotation> annotation(final String annotationName) {
        return annotations.stream().filter(a -> a.name().equals(annotationName)).findFirst();
    }

    /**
     * A resolved statement of a test.
     */
    public sealed interface Statement {
    }

    /**
     * A comment line, copied into the generated test.
     *
     * @param text     the text without the comment markers
     * @param trailing whether it stands behind the previous statement on the same line
     */
    public record Comment(String text, boolean trailing) implements Statement {
    }

    /**
     * Binds a value to a name.
     *
     * @param name     the name
     * @param type     the declared or inferred type
     * @param nullable whether the value may be {@code null}
     * @param value    the value
     */
    public record Let(String name, Type type, boolean nullable, InstanceExpression value) implements Statement {
    }

    /**
     * Evaluates an expression, usually a call of an assertion.
     *
     * @param expression    the expression
     * @param expectedError the error id the expression must fail with ({@code @@expectThrows}), {@code null} if none
     */
    public record Evaluate(InstanceExpression expression, @Nullable String expectedError) implements Statement {
    }

    /**
     * Runs one of two blocks.
     *
     * @param condition the condition
     * @param then      the statements run if the condition is true
     * @param otherwise the statements run if it is false
     */
    public record If(InstanceExpression condition, List<Statement> then, List<Statement> otherwise)
            implements Statement {

        /**
         * Creates a conditional statement.
         *
         * @param condition the condition
         * @param then      the true branch
         * @param otherwise the false branch
         */
        public If {
            then = List.copyOf(then);
            otherwise = List.copyOf(otherwise);
        }
    }

    /**
     * Runs a block once per element of a list or set.
     *
     * @param variable the name bound to the element
     * @param element  the type of the elements
     * @param iterable the list or set
     * @param body     the statements
     */
    public record For(String variable, Type element, InstanceExpression iterable, List<Statement> body)
            implements Statement {

        /**
         * Creates a loop.
         *
         * @param variable the variable
         * @param element  the element type
         * @param iterable the list or set
         * @param body     the statements
         */
        public For {
            body = List.copyOf(body);
        }
    }
}
