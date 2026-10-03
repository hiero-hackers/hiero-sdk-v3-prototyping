package org.hiero.sdk.v3.metalang.ast;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * An annotation such as {@code @@immutable} or {@code @@throws(not-found-error)}.
 *
 * @param name          the name without the {@code @@} prefix
 * @param arguments     the arguments (empty for marker annotations)
 * @param parenthesized whether the annotation was written with parentheses (also {@code @@name()})
 * @param location      the source location
 */
public record Annotation(String name, List<Literal> arguments, boolean parenthesized, SourceLocation location)
        implements Node {

    /**
     * Creates a new annotation.
     *
     * @param name          the name without prefix
     * @param arguments     the arguments
     * @param parenthesized whether parentheses were written
     * @param location      the source location
     */
    public Annotation {
        Objects.requireNonNull(name, "name must not be null");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        Objects.requireNonNull(location, "location must not be null");
    }
}
