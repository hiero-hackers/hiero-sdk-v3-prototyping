package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A resolved method, function-type or enum-attribute parameter.
 *
 * @param name        the name
 * @param type        the resolved type
 * @param varargs     whether it is a varargs parameter
 * @param annotations the annotations
 * @param location    the source location
 */
public record ParameterDefinition(String name, Type type, boolean varargs, List<Annotation> annotations,
                                  SourceLocation location) implements Annotated {

    /**
     * Creates a parameter definition.
     *
     * @param name        the name
     * @param type        the type
     * @param varargs     whether it is varargs
     * @param annotations the annotations
     * @param location    the source location
     */
    public ParameterDefinition {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Returns a copy with another type (used for substitution of type variables).
     *
     * @param newType the new type
     * @return the copy
     */
    public ParameterDefinition withType(final Type newType) {
        return new ParameterDefinition(name, newType, varargs, annotations, location);
    }
}
