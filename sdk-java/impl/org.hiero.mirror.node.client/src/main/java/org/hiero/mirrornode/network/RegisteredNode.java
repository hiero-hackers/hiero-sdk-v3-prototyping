package org.hiero.mirrornode.network;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;

/// A node registered in the network that is not a consensus node, e.g. a block node or a mirror node.
/// Returned by NetworkRepository.registeredNodes() (GET /network/registered-nodes).
///
/// @param timestamp the period this entry is valid for
public record RegisteredNode(
        long registeredNodeId,
        @Nullable Authority adminKey,
        @Nullable ZonedDateTime createdTimestamp,
        @Nullable String description,
        List<RegisteredServiceEndpoint> serviceEndpoints,
        TimestampRange timestamp) {

    /// Creates a new `RegisteredNode`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public RegisteredNode {
        Objects.requireNonNull(serviceEndpoints, "serviceEndpoints must not be null");
        serviceEndpoints = List.copyOf(serviceEndpoints);
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    /// Creates a new `RegisteredNode` with the default value of `serviceEndpoints`.
    public RegisteredNode(
            final long registeredNodeId,
            final @Nullable Authority adminKey,
            final @Nullable ZonedDateTime createdTimestamp,
            final @Nullable String description,
            final TimestampRange timestamp) {
        this(registeredNodeId, adminKey, createdTimestamp, description, List.of(), timestamp);
    }

    /// Returns a builder for a new `RegisteredNode`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `RegisteredNode`.
    ///
    /// `build()` creates the `RegisteredNode` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long registeredNodeId;
        private @Nullable Authority adminKey;
        private @Nullable ZonedDateTime createdTimestamp;
        private @Nullable String description;
        private List<RegisteredServiceEndpoint> serviceEndpoints = List.of();
        private @Nullable TimestampRange timestamp;

        private Builder() {
            // created by RegisteredNode.builder()
        }

        /// Sets the `registeredNodeId`.
        ///
        /// @param registeredNodeId the value
        /// @return this builder
        public Builder registeredNodeId(final long registeredNodeId) {
            this.registeredNodeId = registeredNodeId;
            return this;
        }

        /// Sets the `adminKey`.
        ///
        /// @param adminKey the value
        /// @return this builder
        public Builder adminKey(final @Nullable Authority adminKey) {
            this.adminKey = adminKey;
            return this;
        }

        /// Sets the `createdTimestamp`.
        ///
        /// @param createdTimestamp the value
        /// @return this builder
        public Builder createdTimestamp(final @Nullable ZonedDateTime createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
            return this;
        }

        /// Sets the `description`.
        ///
        /// @param description the value
        /// @return this builder
        public Builder description(final @Nullable String description) {
            this.description = description;
            return this;
        }

        /// Sets the `serviceEndpoints`.
        ///
        /// @param serviceEndpoints the value
        /// @return this builder
        public Builder serviceEndpoints(final List<RegisteredServiceEndpoint> serviceEndpoints) {
            this.serviceEndpoints = serviceEndpoints;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// the period this entry is valid for
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final TimestampRange timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `RegisteredNode` of the values set.
        ///
        /// @return the new `RegisteredNode`
        /// @throws NullPointerException if a required value is not set
        public RegisteredNode build() {
            return new RegisteredNode(
                    Objects.requireNonNull(registeredNodeId, "registeredNodeId must not be null"),
                    adminKey,
                    createdTimestamp,
                    description,
                    serviceEndpoints,
                    Objects.requireNonNull(timestamp, "timestamp must not be null"));
        }
    }
}
