package org.hiero.consensusnode.client;

import java.util.Objects;
import org.hiero.consensusnode.status.TransactionStatus;

/// The node answered: the status of its precheck, or the status of the requested receipt.
///
/// @param status the status the node reported
public record NodeAnswer(TransactionStatus status) implements AttemptResult {

    /// Creates a new `NodeAnswer`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NodeAnswer {
        Objects.requireNonNull(status, "status must not be null");
    }
}
