package org.hiero.sdk.v3.metalang.model;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.Annotated;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A resolved namespace-level constant.
 *
 * @param name          the qualified name
 * @param type          the resolved type
 * @param value         the value
 * @param annotations   the annotations
 * @param documentation the documentation
 * @param location      the source location
 */
public record ConstantDefinition(QualifiedName name, Type type, Literal value, List<Annotation> annotations,
                                 String documentation, SourceLocation location) implements Annotated {

    /**
     * Creates a constant definition.
     *
     * @param name          the qualified name
     * @param type          the type
     * @param value         the value
     * @param annotations   the annotations
     * @param documentation the documentation
     * @param location      the source location
     */
    public ConstantDefinition {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(value, "value must not be null");
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(documentation, "documentation must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
