package org.hiero.mirrornode.network;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// One consensus-node entry in the address book. Returned by NetworkRepository.nodes()
/// (GET /network/nodes).
///
/// @param nodeId stable HIP-869 node id
/// @param accountId fee account that receives this node's share
/// @param publicKey node gossip public key (hex)
/// @param nodeCertHash SHA-384 of the node's TLS certificate (hex)
/// @param adminKey DAB admin key (HIP-869); null on pre-DAB entries
/// @param stake total stake delegated to this node (tinybars)
/// @param timestamp consensus time of this address-book entry
public record NetworkNode(
        long nodeId,
        AccountId accountId,
        @Nullable String description,
        @Nullable String publicKey,
        @Nullable String nodeCertHash,
        List<ServiceEndpoint> serviceEndpoints,
        @Nullable Authority adminKey,
        long stake,
        @Nullable ZonedDateTime timestamp) {

    /// Creates a new `NetworkNode`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NetworkNode {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(serviceEndpoints, "serviceEndpoints must not be null");
        serviceEndpoints = List.copyOf(serviceEndpoints);
    }

    /// Creates a new `NetworkNode` with the default value of `serviceEndpoints`.
    public NetworkNode(
            final long nodeId,
            final AccountId accountId,
            final @Nullable String description,
            final @Nullable String publicKey,
            final @Nullable String nodeCertHash,
            final @Nullable Authority adminKey,
            final long stake,
            final @Nullable ZonedDateTime timestamp) {
        this(nodeId, accountId, description, publicKey, nodeCertHash, List.of(), adminKey, stake, timestamp);
    }

    /// Returns a builder for a new `NetworkNode`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NetworkNode`.
    ///
    /// `build()` creates the `NetworkNode` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long nodeId;
        private @Nullable AccountId accountId;
        private @Nullable String description;
        private @Nullable String publicKey;
        private @Nullable String nodeCertHash;
        private List<ServiceEndpoint> serviceEndpoints = List.of();
        private @Nullable Authority adminKey;
        private @Nullable Long stake;
        private @Nullable ZonedDateTime timestamp;

        private Builder() {
            // created by NetworkNode.builder()
        }

        /// Sets the `nodeId`.
        ///
        /// stable HIP-869 node id
        ///
        /// @param nodeId the value
        /// @return this builder
        public Builder nodeId(final long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// fee account that receives this node's share
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final AccountId accountId) {
            this.accountId = accountId;
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

        /// Sets the `publicKey`.
        ///
        /// node gossip public key (hex)
        ///
        /// @param publicKey the value
        /// @return this builder
        public Builder publicKey(final @Nullable String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        /// Sets the `nodeCertHash`.
        ///
        /// SHA-384 of the node's TLS certificate (hex)
        ///
        /// @param nodeCertHash the value
        /// @return this builder
        public Builder nodeCertHash(final @Nullable String nodeCertHash) {
            this.nodeCertHash = nodeCertHash;
            return this;
        }

        /// Sets the `serviceEndpoints`.
        ///
        /// @param serviceEndpoints the value
        /// @return this builder
        public Builder serviceEndpoints(final List<ServiceEndpoint> serviceEndpoints) {
            this.serviceEndpoints = serviceEndpoints;
            return this;
        }

        /// Sets the `adminKey`.
        ///
        /// DAB admin key (HIP-869); null on pre-DAB entries
        ///
        /// @param adminKey the value
        /// @return this builder
        public Builder adminKey(final @Nullable Authority adminKey) {
            this.adminKey = adminKey;
            return this;
        }

        /// Sets the `stake`.
        ///
        /// total stake delegated to this node (tinybars)
        ///
        /// @param stake the value
        /// @return this builder
        public Builder stake(final long stake) {
            this.stake = stake;
            return this;
        }

        /// Sets the `timestamp`.
        ///
        /// consensus time of this address-book entry
        ///
        /// @param timestamp the value
        /// @return this builder
        public Builder timestamp(final @Nullable ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /// Creates the `NetworkNode` of the values set.
        ///
        /// @return the new `NetworkNode`
        /// @throws NullPointerException if a required value is not set
        public NetworkNode build() {
            return new NetworkNode(
                    Objects.requireNonNull(nodeId, "nodeId must not be null"),
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    description,
                    publicKey,
                    nodeCertHash,
                    serviceEndpoints,
                    adminKey,
                    Objects.requireNonNull(stake, "stake must not be null"),
                    timestamp);
        }
    }
}
