package org.hiero.sdk.v3.metalang.ast;

import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A source comment of a schema (line or block comment), kept for checks that inspect comments.
 *
 * @param text     the comment text without comment markers
 * @param location the source location
 */
public record Comment(String text, SourceLocation location) implements Node {

    /**
     * Creates a comment.
     *
     * @param text     the text
     * @param location the source location
     */
    public Comment {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
