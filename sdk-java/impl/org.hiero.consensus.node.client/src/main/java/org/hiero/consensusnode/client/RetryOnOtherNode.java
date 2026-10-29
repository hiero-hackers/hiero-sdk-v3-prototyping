package org.hiero.consensusnode.client;

import java.time.Duration;
import java.util.Objects;

/// Try again on another node after the delay. The `NodeSelector` picks the node among the candidates of the request
/// other than the node of the last attempt; when there is no other candidate, the same node is used.
///
/// @param delay the time to wait before the next attempt
public record RetryOnOtherNode(Duration delay) implements RetryDecision {

    /// Creates a new `RetryOnOtherNode`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public RetryOnOtherNode {
        Objects.requireNonNull(delay, "delay must not be null");
    }
}
