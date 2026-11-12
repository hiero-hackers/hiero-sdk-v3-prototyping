package org.hiero.consensusnode.client;

import java.io.Serial;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// Signals an unknown status error.
public final class UnknownStatusException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Creates a new `UnknownStatusException`.
    ///
    /// @param message the detail message
    /// @param cause   the cause, or `null` if there is none
    /// @throws NullPointerException if `message` is `null`
    public UnknownStatusException(final String message, final @Nullable Throwable cause) {
        super(Objects.requireNonNull(message, "message must not be null"), cause);
    }
}
