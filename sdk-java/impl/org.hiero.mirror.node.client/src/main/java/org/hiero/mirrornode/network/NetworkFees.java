package org.hiero.mirrornode.network;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The gas fee schedule of the network.
///
/// @param timestamp consensus time at which the fee schedule was valid
public record NetworkFees(List<NetworkFee> fees, @Nullable ZonedDateTime timestamp) {

    /// Creates a new `NetworkFees`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NetworkFees {
        Objects.requireNonNull(fees, "fees must not be null");
        fees = List.copyOf(fees);
    }

    /// Creates a new `NetworkFees` with the default value of `fees`.
    public NetworkFees(final @Nullable ZonedDateTime timestamp) {
        this(List.of(), timestamp);
    }

    /// Returns a builder for a new `NetworkFees`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NetworkFees`.
    ///
    /// `build()` creates the `NetworkFees` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private List<NetworkFee> fees = List.of();
        private @Nullable ZonedDateTime timestamp;

        private Builder() {
            // created by NetworkFees.builder()
        }

        /// Sets the `fees`.
        ///
        /// @param fees the value
        /// @return this builder
        public Builder fees(final List<NetworkFee> fees) {
            this.fees = fees;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// consensus time at which the fee schedule was valid
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final @Nullable ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `NetworkFees` of the values set.
        ///
        /// @return the new `NetworkFees`
        public NetworkFees build() {
            return new NetworkFees(fees, timestamp);
        }
    }
}
