package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A value of an enum with its attribute values (in the order of the enum's attribute list).
 *
 * @param name          the value name
 * @param arguments     the attribute values
 * @param annotations   the annotations
 * @param documentation the documentation
 * @param location      the source location
 */
public record EnumValueDefinition(String name, List<Literal> arguments, List<Annotation> annotations,
                                  String documentation, SourceLocation location) implements Annotated {

    /**
     * Creates an enum value definition.
     *
     * @param name          the name
     * @param arguments     the attribute values
     * @param annotations   the annotations
     * @param documentation the documentation
     * @param location      the source location
     */
    public EnumValueDefinition {
        Objects.requireNonNull(name, "name must not be null");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
