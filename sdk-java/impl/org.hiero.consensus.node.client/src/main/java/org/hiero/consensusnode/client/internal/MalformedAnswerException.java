package org.hiero.consensusnode.client.internal;

import java.io.Serial;
import org.jspecify.annotations.Nullable;

/// A node answered a request successfully, but the value of its answer cannot be read, e.g. the contents of a system
/// file that cannot be decoded. The attempt is classified `REJECTED`.
public final class MalformedAnswerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Creates the error.
    ///
    /// @param message a description for diagnosis
    /// @param cause   the cause, or `null` if there is none
    public MalformedAnswerException(final String message, final @Nullable Throwable cause) {
        super(message, cause);
    }
}
