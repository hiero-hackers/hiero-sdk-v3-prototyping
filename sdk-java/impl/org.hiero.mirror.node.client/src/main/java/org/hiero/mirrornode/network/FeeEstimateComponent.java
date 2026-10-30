package org.hiero.mirrornode.network;

import java.util.List;
import java.util.Objects;

/// The node or the service component of a fee estimate.
///
/// @param base base fee in tinycents
public record FeeEstimateComponent(long base, List<FeeExtra> extras) {

    /// Creates a new `FeeEstimateComponent`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FeeEstimateComponent {
        Objects.requireNonNull(extras, "extras must not be null");
        extras = List.copyOf(extras);
    }

    /// Creates a new `FeeEstimateComponent` with the default value of `extras`.
    public FeeEstimateComponent(final long base) {
        this(base, List.of());
    }
}
