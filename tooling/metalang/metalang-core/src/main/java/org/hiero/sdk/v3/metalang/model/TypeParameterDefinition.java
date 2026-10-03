package org.hiero.sdk.v3.metalang.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A declared generic type parameter of a type or method.
 *
 * @param variable the type variable it declares
 * @param bound    the resolved upper bound or {@code null}
 */
public record TypeParameterDefinition(Type.TypeVariable variable, @Nullable Type bound) {

    /**
     * Creates a type parameter definition.
     *
     * @param variable the declared variable
     * @param bound    the bound or {@code null}
     */
    public TypeParameterDefinition {
        Objects.requireNonNull(variable, "variable must not be null");
    }

    /**
     * Returns the parameter name including the {@code $$} prefix.
     *
     * @return the name
     */
    public String name() {
        return variable.name();
    }
}
