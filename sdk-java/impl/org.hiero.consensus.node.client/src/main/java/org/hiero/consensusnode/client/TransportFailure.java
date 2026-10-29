package org.hiero.consensusnode.client;

import java.util.Objects;

/// The node did not answer.
///
/// @param kind why no answer arrived
/// @param message a description for diagnosis and logs; not meant to be evaluated
public record TransportFailure(TransportFailureKind kind, String message) implements AttemptResult {

    /// Creates a new `TransportFailure`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TransportFailure {
        Objects.requireNonNull(kind, "kind must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
