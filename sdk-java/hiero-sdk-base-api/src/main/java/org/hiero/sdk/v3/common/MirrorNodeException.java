package org.hiero.sdk.v3.common;

import java.io.Serial;
import org.jspecify.annotations.Nullable;

/** Indicates that a Mirror Node request could not produce the requested page. */
public class MirrorNodeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message, or {@code null} when none is supplied
     */
    public MirrorNodeException(@Nullable final String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and underlying cause.
     *
     * @param message the detail message, or {@code null} when none is supplied
     * @param cause the underlying cause, or {@code null} when unknown
     */
    public MirrorNodeException(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
