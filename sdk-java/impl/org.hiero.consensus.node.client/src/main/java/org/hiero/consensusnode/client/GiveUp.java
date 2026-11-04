package org.hiero.consensusnode.client;

import org.jspecify.annotations.Nullable;


/// Do not try again: the request fails with `attempts-exhausted-error`.
public final class GiveUp implements RetryDecision {

    /// Creates a new `GiveUp`.
    public GiveUp() {
    }

    /// All instances are equal: the type has no attributes.
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj != null && obj.getClass() == getClass();
    }

    /// The same for all instances, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }
}
