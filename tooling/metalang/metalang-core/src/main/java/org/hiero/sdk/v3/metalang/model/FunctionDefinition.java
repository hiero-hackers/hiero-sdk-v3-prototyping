package org.hiero.sdk.v3.metalang.model;

import java.util.Objects;

/**
 * A namespace-level function.
 *
 * @param namespace the declaring namespace
 * @param method    the resolved signature ({@link MethodDefinition#declaringType()} is {@code null})
 */
public record FunctionDefinition(String namespace, MethodDefinition method) {

    /**
     * Creates a function definition.
     *
     * @param namespace the namespace
     * @param method    the signature
     */
    public FunctionDefinition {
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(method, "method must not be null");
    }
}
