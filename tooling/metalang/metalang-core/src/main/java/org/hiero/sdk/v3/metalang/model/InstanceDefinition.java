package org.hiero.sdk.v3.metalang.model;

import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A resolved default instance: the standard way to obtain an instance of a type through the API.
 *
 * @param type          the type, with its type arguments as declared ({@code HieroClient<ANY>})
 * @param expression    how the instance is obtained
 * @param documentation the comment above the declaration
 * @param location      the location of the declaration
 */
public record InstanceDefinition(Type.DeclaredType type, InstanceExpression expression, String documentation,
                                 SourceLocation location) {

    /**
     * Creates a default instance.
     *
     * @param type          the type
     * @param expression    the expression
     * @param documentation the documentation
     * @param location      the location
     */
    public InstanceDefinition {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
