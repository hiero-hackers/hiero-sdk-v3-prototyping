package org.hiero.sdk.v3.common;

import java.io.Serial;

/** Indicates that a Mirror Node request could not produce the requested page. */
public class MirrorNodeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a detail message.
     *
     * @param message the detail message
     */
    public MirrorNodeException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with a detail message and underlying cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public MirrorNodeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
