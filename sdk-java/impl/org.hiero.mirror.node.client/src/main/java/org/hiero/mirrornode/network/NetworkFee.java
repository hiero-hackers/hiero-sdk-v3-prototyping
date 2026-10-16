package org.hiero.mirrornode.network;

import java.util.Objects;

public record NetworkFee(long gas, String transactionType) {

    /// Creates a new `NetworkFee`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NetworkFee {
        Objects.requireNonNull(transactionType, "transactionType must not be null");
    }
}
