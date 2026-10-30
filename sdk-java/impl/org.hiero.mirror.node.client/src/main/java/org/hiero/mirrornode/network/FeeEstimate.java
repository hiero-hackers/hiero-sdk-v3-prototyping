package org.hiero.mirrornode.network;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// Estimated fee for a candidate transaction (HIP-1313, congestion-adjusted). Returned by
/// NetworkRepository.estimateFees().
///
/// @param node paid to the node that submitted the transaction
/// @param service execution and storage
/// @param total estimated total fee in tinycents
public record FeeEstimate(
        long highVolumeMultiplier,
        FeeEstimateNetwork network,
        FeeEstimateComponent node,
        FeeEstimateComponent service,
        long total) {

    /// Creates a new `FeeEstimate`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public FeeEstimate {
        Objects.requireNonNull(network, "network must not be null");
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(service, "service must not be null");
    }

    /// Returns a builder for a new `FeeEstimate`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `FeeEstimate`.
    ///
    /// `build()` creates the `FeeEstimate` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long highVolumeMultiplier;
        private @Nullable FeeEstimateNetwork network;
        private @Nullable FeeEstimateComponent node;
        private @Nullable FeeEstimateComponent service;
        private @Nullable Long total;

        private Builder() {
            // created by FeeEstimate.builder()
        }

        /// Sets the `highVolumeMultiplier`.
        ///
        /// @param highVolumeMultiplier the value
        /// @return this builder
        public Builder highVolumeMultiplier(final long highVolumeMultiplier) {
            this.highVolumeMultiplier = highVolumeMultiplier;
            return this;
        }

        /// Sets the `network`.
        ///
        /// @param network the value
        /// @return this builder
        public Builder network(final FeeEstimateNetwork network) {
            this.network = network;
            return this;
        }

        /// Sets the `node`.
        ///
        /// paid to the node that submitted the transaction
        ///
        /// @param node the value
        /// @return this builder
        public Builder node(final FeeEstimateComponent node) {
            this.node = node;
            return this;
        }

        /// Sets the `service`.
        ///
        /// execution and storage
        ///
        /// @param service the value
        /// @return this builder
        public Builder service(final FeeEstimateComponent service) {
            this.service = service;
            return this;
        }

        /// Sets the `total`.
        ///
        /// estimated total fee in tinycents
        ///
        /// @param total the value
        /// @return this builder
        public Builder total(final long total) {
            this.total = total;
            return this;
        }

        /// Creates the `FeeEstimate` of the values set.
        ///
        /// @return the new `FeeEstimate`
        /// @throws NullPointerException if a required value is not set
        public FeeEstimate build() {
            return new FeeEstimate(
                    Objects.requireNonNull(highVolumeMultiplier, "highVolumeMultiplier must not be null"),
                    Objects.requireNonNull(network, "network must not be null"),
                    Objects.requireNonNull(node, "node must not be null"),
                    Objects.requireNonNull(service, "service must not be null"),
                    Objects.requireNonNull(total, "total must not be null"));
        }
    }
}
