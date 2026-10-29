package org.hiero.consensusnode.client.internal;

import java.io.Serial;
import java.util.Objects;
import org.hiero.consensusnode.client.TransportFailureKind;

/// No answer of a node arrived: the transport failed or the attempt timed out.
public final class TransportException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Why no answer arrived.
    private final TransportFailureKind kind;

    /// Creates the error.
    ///
    /// @param kind    why no answer arrived
    /// @param message a description for diagnosis
    public TransportException(final TransportFailureKind kind, final String message) {
        super(message);
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
    }

    /// Why no answer arrived.
    ///
    /// @return the kind
    public TransportFailureKind kind() {
        return kind;
    }
}
