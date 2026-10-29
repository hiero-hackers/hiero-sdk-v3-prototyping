package org.hiero.consensusnode.client;


/// Do not try again: the request fails with `attempts-exhausted-error`.
public final class GiveUp implements RetryDecision {

    /// Creates a new `GiveUp`.
    public GiveUp() {
    }
}
