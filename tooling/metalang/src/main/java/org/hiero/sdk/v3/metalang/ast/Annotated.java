package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Optional;

/**
 * A node that can carry {@code @@} annotations.
 */
public interface Annotated extends Node {

    /**
     * Returns the annotations in declaration order.
     *
     * @return the annotations
     */
    List<Annotation> annotations();

    /**
     * Returns the first annotation with the given name.
     *
     * @param name annotation name without the {@code @@} prefix
     * @return the annotation, if present
     */
    default Optional<Annotation> annotation(final String name) {
        return annotations().stream().filter(a -> a.name().equals(name)).findFirst();
    }

    /**
     * Checks whether an annotation with the given name is present.
     *
     * @param name annotation name without the {@code @@} prefix
     * @return {@code true} if present
     */
    default boolean hasAnnotation(final String name) {
        return annotation(name).isPresent();
    }
}
