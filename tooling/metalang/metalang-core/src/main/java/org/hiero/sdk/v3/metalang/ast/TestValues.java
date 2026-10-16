package org.hiero.sdk.v3.metalang.ast;

import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * Named test values (a declaration of the "## Default Instances for Testing" block): a value, typically a list of
 * inputs, that the tests of the same spec use by its name, e.g.
 * {@code values MALFORMED_ENTITY_IDS: list<string> = ["", "0.0"]}.
 *
 * @param name          the name the tests use
 * @param type          the declared type
 * @param expression    the value
 * @param documentation the comment above the declaration
 * @param location      the location of the {@code values} keyword
 */
public record TestValues(String name, TypeRef type, Expression expression, String documentation,
                         SourceLocation location) implements Node {

    /**
     * Creates named test values.
     *
     * @param name          the name
     * @param type          the type
     * @param expression    the value
     * @param documentation the documentation
     * @param location      the location
     */
    public TestValues {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
