package org.hiero.sdk.v3.metalang.ast;

import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A default instance (a declaration of the "## Default Instances" block): the standard way to obtain an instance of a
 * type through the API, e.g. {@code instance PublicKey = DEFAULT(PrivateKey).createPublicKey()}.
 *
 * @param type          the type the instance is for
 * @param expression    how the instance is obtained
 * @param documentation the comment above the declaration
 * @param location      the location of the {@code instance} keyword
 */
public record Instance(TypeRef type, Expression expression, String documentation, SourceLocation location)
        implements Node {

    /**
     * Creates a default instance.
     *
     * @param type          the type
     * @param expression    the expression
     * @param documentation the documentation
     * @param location      the location
     */
    public Instance {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
