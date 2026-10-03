package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A method or function-type parameter.
 *
 * @param name        the parameter name
 * @param annotations the annotations
 * @param type        the declared type
 * @param varargs     whether the parameter is declared with {@code ...}
 * @param location    the source location
 */
public record Parameter(String name, List<Annotation> annotations, TypeRef type, boolean varargs,
                        SourceLocation location) implements Annotated {

    /**
     * Creates a parameter.
     *
     * @param name        the name
     * @param annotations the annotations
     * @param type        the type
     * @param varargs     whether it is a varargs parameter
     * @param location    the source location
     */
    public Parameter {
        Objects.requireNonNull(name, "name must not be null");
        annotations = List.copyOf(Objects.requireNonNull(annotations, "annotations must not be null"));
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }

    /**
     * Returns the canonical textual form, e.g. {@code signers: Key...}.
     *
     * @return the text
     */
    public String text() {
        return name + ": " + type.text() + (varargs ? "..." : "");
    }
}
