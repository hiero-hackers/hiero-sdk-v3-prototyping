package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * An attribute of a complex type or enum.
 *
 * @param name          the field name
 * @param annotations   the annotations
 * @param type          the declared type
 * @param documentation the attached comment text (may be empty)
 * @param location      the source location
 */
public record Field(String name, List<Annotation> annotations, TypeRef type, String documentation,
                    SourceLocation location) implements Member {

    /**
     * Creates a field.
     *
     * @param name          the name
     * @param annotations   the annotations
     * @param type          the type
     * @param documentation the documentation
     * @param location      the source location
     */
    public Field {
        Objects.requireNonNull(name, "name must not be null");
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
