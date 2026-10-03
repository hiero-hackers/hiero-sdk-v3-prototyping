package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A resolved attribute. In the effective members of a type, inherited attributes appear with the type arguments of
 * the supertype substituted; {@link #declaringType()} still names the type that declares the attribute.
 *
 * @param name          the name
 * @param type          the resolved (and, for inherited attributes, substituted) type
 * @param declaringType the type that declares the attribute
 * @param annotations   the annotations
 * @param documentation the documentation
 * @param location      the source location of the declaration
 */
public record FieldDefinition(String name, Type type, QualifiedName declaringType, List<Annotation> annotations,
                              String documentation, SourceLocation location) implements Annotated {

    /**
     * Creates a field definition.
     *
     * @param name          the name
     * @param type          the type
     * @param declaringType the declaring type
     * @param annotations   the annotations
     * @param documentation the documentation
     * @param location      the source location
     */
    public FieldDefinition {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(declaringType, "declaringType must not be null");
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Returns a copy with another type (used for substitution of type variables).
     *
     * @param newType the new type
     * @return the copy
     */
    public FieldDefinition withType(final Type newType) {
        return new FieldDefinition(name, newType, declaringType, annotations, documentation, location);
    }
}
