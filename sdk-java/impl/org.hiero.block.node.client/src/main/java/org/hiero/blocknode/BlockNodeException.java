package org.hiero.blocknode;

import java.io.Serial;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// Signals a block node error.
public final class BlockNodeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Creates a new `BlockNodeException`.
    ///
    /// @param message the detail message
    /// @param cause   the cause, or `null` if there is none
    /// @throws NullPointerException if `message` is `null`
    public BlockNodeException(final String message, final @Nullable Throwable cause) {
        super(Objects.requireNonNull(message, "message must not be null"), cause);
    }
}
