package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A value of an enumeration.
 *
 * @param name          the value name
 * @param annotations   the annotations
 * @param documentation the documentation
 * @param location      the source location
 */
public record EnumValue(String name, List<Annotation> annotations, String documentation, SourceLocation location)
        implements Annotated {

    /**
     * Creates an enum value.
     *
     * @param name          the name
     * @param annotations   the annotations
     * @param documentation the documentation
     * @param location      the source location
     */
    public EnumValue {
        Objects.requireNonNull(name, "name must not be null");
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
