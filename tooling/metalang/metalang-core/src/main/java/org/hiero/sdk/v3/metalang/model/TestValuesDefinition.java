package org.hiero.sdk.v3.metalang.model;

import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * Resolved named test values of a spec ({@code values NAME: type = expression} in its
 * {@code ## Default Instances for Testing} section): a value that the tests of the same spec use by its name.
 *
 * @param name          the name
 * @param namespace     the namespace of the spec
 * @param specName      the name of the spec file without directory and extension, e.g. {@code native-token}
 * @param type          the declared type
 * @param value         the resolved value
 * @param documentation the comment above the declaration
 * @param location      the location of the declaration
 */
public record TestValuesDefinition(String name, String namespace, String specName, Type type,
                                   InstanceExpression value, String documentation, SourceLocation location) {

    /**
     * Creates named test values.
     *
     * @param name          the name
     * @param namespace     the namespace
     * @param specName      the spec file name
     * @param type          the type
     * @param value         the value
     * @param documentation the documentation
     * @param location      the location
     */
    public TestValuesDefinition {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(specName, "specName must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
