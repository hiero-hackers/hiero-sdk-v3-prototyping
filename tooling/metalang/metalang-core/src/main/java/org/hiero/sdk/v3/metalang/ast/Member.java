package org.hiero.sdk.v3.metalang.ast;

/**
 * A member of a complex type or enum: a {@link Field} or a {@link Method}.
 */
public sealed interface Member extends Annotated permits Field, Method {

    /**
     * Returns the member name.
     *
     * @return the name
     */
    String name();

    /**
     * Returns the attached documentation comment (may be empty).
     *
     * @return the documentation
     */
    String documentation();
}
