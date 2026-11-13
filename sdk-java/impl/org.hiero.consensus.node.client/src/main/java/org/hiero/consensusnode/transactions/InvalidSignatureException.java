package org.hiero.consensusnode.transactions;

import java.io.Serial;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// Signals an invalid signature error.
public final class InvalidSignatureException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Creates a new `InvalidSignatureException`.
    ///
    /// @param message the detail message
    /// @param cause   the cause, or `null` if there is none
    /// @throws NullPointerException if `message` is `null`
    public InvalidSignatureException(final String message, final @Nullable Throwable cause) {
        super(Objects.requireNonNull(message, "message must not be null"), cause);
    }
}
