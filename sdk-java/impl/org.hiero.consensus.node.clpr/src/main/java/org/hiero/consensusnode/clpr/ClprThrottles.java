package org.hiero.consensusnode.clpr;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The capacity limits a ledger enforces on its CLPR service. maxSyncBytes must be at least
/// maxMessagePayloadBytes + 65,536 bytes; the network rejects a configuration that violates this with
/// INVALID_CLPR_CONFIGURATION.
///
/// @param maxMessagesPerBundle the maximum number of messages in one bundle
/// @param maxMessagePayloadBytes the maximum size of the payload of one message, in bytes
/// @param maxGasPerMessage the maximum gas the execution of one message may use
/// @param maxQueueDepth the maximum number of outbound messages a Channel may queue
/// @param maxSyncBytes the maximum size of one bundle, in bytes
/// @param maxLocalEndpoints the maximum number of endpoints of this ledger; 0: no limit
/// @param maxPeerEndpoints the maximum number of endpoints of a peer ledger; 0: no limit
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public record ClprThrottles(
        long maxMessagesPerBundle,
        long maxMessagePayloadBytes,
        long maxGasPerMessage,
        long maxQueueDepth,
        long maxSyncBytes,
        long maxLocalEndpoints,
        long maxPeerEndpoints) {

    /// Creates a new `ClprThrottles`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public ClprThrottles {
        if (maxMessagesPerBundle < 0L || maxMessagesPerBundle > 4294967295L) {
            throw new IllegalArgumentException("maxMessagesPerBundle must be between 0 and 4294967295");
        }
        if (maxMessagesPerBundle < 1L) {
            throw new IllegalArgumentException("maxMessagesPerBundle must be at least 1");
        }
        if (maxMessagePayloadBytes < 0L || maxMessagePayloadBytes > 4294967295L) {
            throw new IllegalArgumentException("maxMessagePayloadBytes must be between 0 and 4294967295");
        }
        if (maxMessagePayloadBytes < 1L) {
            throw new IllegalArgumentException("maxMessagePayloadBytes must be at least 1");
        }
        if (Long.compareUnsigned(maxGasPerMessage, 1L) < 0) {
            throw new IllegalArgumentException("maxGasPerMessage must be at least 1");
        }
        if (maxQueueDepth < 0L || maxQueueDepth > 4294967295L) {
            throw new IllegalArgumentException("maxQueueDepth must be between 0 and 4294967295");
        }
        if (maxQueueDepth < 1L) {
            throw new IllegalArgumentException("maxQueueDepth must be at least 1");
        }
        if (Long.compareUnsigned(maxSyncBytes, 1L) < 0) {
            throw new IllegalArgumentException("maxSyncBytes must be at least 1");
        }
        if (maxLocalEndpoints < 0L || maxLocalEndpoints > 4294967295L) {
            throw new IllegalArgumentException("maxLocalEndpoints must be between 0 and 4294967295");
        }
        if (maxPeerEndpoints < 0L || maxPeerEndpoints > 4294967295L) {
            throw new IllegalArgumentException("maxPeerEndpoints must be between 0 and 4294967295");
        }
    }

    /// Returns a builder for a new `ClprThrottles`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `ClprThrottles`.
    ///
    /// `build()` creates the `ClprThrottles` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long maxMessagesPerBundle;
        private @Nullable Long maxMessagePayloadBytes;
        private @Nullable Long maxGasPerMessage;
        private @Nullable Long maxQueueDepth;
        private @Nullable Long maxSyncBytes;
        private @Nullable Long maxLocalEndpoints;
        private @Nullable Long maxPeerEndpoints;

        private Builder() {
            // created by ClprThrottles.builder()
        }

        /// Sets the `maxMessagesPerBundle`.
        ///
        /// the maximum number of messages in one bundle
        ///
        /// @param maxMessagesPerBundle the value
        /// @return this builder
        public Builder maxMessagesPerBundle(final long maxMessagesPerBundle) {
            this.maxMessagesPerBundle = maxMessagesPerBundle;
            return this;
        }

        /// Sets the `maxMessagePayloadBytes`.
        ///
        /// the maximum size of the payload of one message, in bytes
        ///
        /// @param maxMessagePayloadBytes the value
        /// @return this builder
        public Builder maxMessagePayloadBytes(final long maxMessagePayloadBytes) {
            this.maxMessagePayloadBytes = maxMessagePayloadBytes;
            return this;
        }

        /// Sets the `maxGasPerMessage`.
        ///
        /// the maximum gas the execution of one message may use
        ///
        /// @param maxGasPerMessage the value
        /// @return this builder
        public Builder maxGasPerMessage(final long maxGasPerMessage) {
            this.maxGasPerMessage = maxGasPerMessage;
            return this;
        }

        /// Sets the `maxQueueDepth`.
        ///
        /// the maximum number of outbound messages a Channel may queue
        ///
        /// @param maxQueueDepth the value
        /// @return this builder
        public Builder maxQueueDepth(final long maxQueueDepth) {
            this.maxQueueDepth = maxQueueDepth;
            return this;
        }

        /// Sets the `maxSyncBytes`.
        ///
        /// the maximum size of one bundle, in bytes
        ///
        /// @param maxSyncBytes the value
        /// @return this builder
        public Builder maxSyncBytes(final long maxSyncBytes) {
            this.maxSyncBytes = maxSyncBytes;
            return this;
        }

        /// Sets the `maxLocalEndpoints`.
        ///
        /// the maximum number of endpoints of this ledger; 0: no limit
        ///
        /// @param maxLocalEndpoints the value
        /// @return this builder
        public Builder maxLocalEndpoints(final long maxLocalEndpoints) {
            this.maxLocalEndpoints = maxLocalEndpoints;
            return this;
        }

        /// Sets the `maxPeerEndpoints`.
        ///
        /// the maximum number of endpoints of a peer ledger; 0: no limit
        ///
        /// @param maxPeerEndpoints the value
        /// @return this builder
        public Builder maxPeerEndpoints(final long maxPeerEndpoints) {
            this.maxPeerEndpoints = maxPeerEndpoints;
            return this;
        }

        /// Creates the `ClprThrottles` of the values set.
        ///
        /// @return the new `ClprThrottles`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public ClprThrottles build() {
            return new ClprThrottles(
                    Objects.requireNonNull(maxMessagesPerBundle, "maxMessagesPerBundle must not be null"),
                    Objects.requireNonNull(maxMessagePayloadBytes, "maxMessagePayloadBytes must not be null"),
                    Objects.requireNonNull(maxGasPerMessage, "maxGasPerMessage must not be null"),
                    Objects.requireNonNull(maxQueueDepth, "maxQueueDepth must not be null"),
                    Objects.requireNonNull(maxSyncBytes, "maxSyncBytes must not be null"),
                    Objects.requireNonNull(maxLocalEndpoints, "maxLocalEndpoints must not be null"),
                    Objects.requireNonNull(maxPeerEndpoints, "maxPeerEndpoints must not be null"));
        }
    }
}
