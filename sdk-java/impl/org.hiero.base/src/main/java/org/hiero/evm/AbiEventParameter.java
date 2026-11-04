package org.hiero.evm;

import java.util.Objects;

/// A parameter of an event.
///
/// @param indexed true: stored as a topic of the log instead of in its data
public record AbiEventParameter(AbiType abiType, boolean indexed) {

    /// Creates a new `AbiEventParameter`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AbiEventParameter {
        Objects.requireNonNull(abiType, "abiType must not be null");
    }

    /// Creates a new `AbiEventParameter` with the default value of `indexed`.
    public AbiEventParameter(final AbiType abiType) {
        this(abiType, false);
    }
}
