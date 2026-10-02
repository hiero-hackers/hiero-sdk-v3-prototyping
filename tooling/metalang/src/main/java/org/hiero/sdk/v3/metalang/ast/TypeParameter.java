package org.hiero.sdk.v3.metalang.ast;

import java.util.Objects;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * Declaration of a generic type parameter, e.g. {@code $$Receipt extends Receipt}.
 *
 * @param name     the name including the {@code $$} prefix
 * @param bound    the upper bound or {@code null}
 * @param location the source location
 */
public record TypeParameter(String name, @Nullable TypeRef bound, SourceLocation location) implements Node {

    /**
     * Creates a type parameter.
     *
     * @param name     the name including prefix
     * @param bound    the bound or {@code null}
     * @param location the source location
     */
    public TypeParameter {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Returns the upper bound.
     *
     * @return the bound, if declared
     */
    public Optional<TypeRef> boundOptional() {
        return Optional.ofNullable(bound);
    }
}
