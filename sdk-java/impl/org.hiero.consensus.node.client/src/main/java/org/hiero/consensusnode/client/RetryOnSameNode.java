package org.hiero.consensusnode.client;

import java.time.Duration;
import java.util.Objects;

/// Try again on the node of the last attempt after the delay.
///
/// @param delay the time to wait before the next attempt
public record RetryOnSameNode(Duration delay) implements RetryDecision {

    /// Creates a new `RetryOnSameNode`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public RetryOnSameNode {
        Objects.requireNonNull(delay, "delay must not be null");
    }
}
