package org.hiero.consensusnode.admin.network;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.hiero.consensusnode.admin.nodes.ServiceEndpoint;
import org.hiero.ledger.AccountId;
import org.jspecify.annotations.Nullable;

/// One address-book entry: the stable node id plus everything a client needs to reach the node and verify its TLS
/// certificates. Read-only counterpart of the data set with `NodeCreateTransaction`.
///
/// @param nodeId stable HIP-869 identifier; survives account / endpoint rotation
/// @param accountId fee account that receives this node's per-transaction share
/// @param serviceEndpoints public gRPC endpoints (clients submit transactions here)
/// @param description free-form description of the node (at most 100 UTF-8 bytes); empty if none
/// @param gossipCaCertificate X.509 DER bytes of the certificate that terminates mutual TLS for gossip
/// @param grpcCertificateHash SHA-384 of the certificate served on serviceEndpoints; clients can pin against this; empty if the node has none
/// @param rsaPublicKey legacy gossip RSA public key (DER); only for tooling that still reads the legacy address-book format — prefer gossipCaCertificate and grpcCertificateHash
public record NodeAddress(
        long nodeId,
        AccountId accountId,
        List<ServiceEndpoint> serviceEndpoints,
        String description,
        byte[] gossipCaCertificate,
        byte[] grpcCertificateHash,
        byte[] rsaPublicKey) {

    /// Creates a new `NodeAddress`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public NodeAddress {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(serviceEndpoints, "serviceEndpoints must not be null");
        serviceEndpoints = List.copyOf(serviceEndpoints);
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(gossipCaCertificate, "gossipCaCertificate must not be null");
        gossipCaCertificate = gossipCaCertificate.clone();
        Objects.requireNonNull(grpcCertificateHash, "grpcCertificateHash must not be null");
        grpcCertificateHash = grpcCertificateHash.clone();
        Objects.requireNonNull(rsaPublicKey, "rsaPublicKey must not be null");
        rsaPublicKey = rsaPublicKey.clone();
    }

    /// Creates a new `NodeAddress` with the default value of `serviceEndpoints`, `description`.
    public NodeAddress(
            final long nodeId,
            final AccountId accountId,
            final byte[] gossipCaCertificate,
            final byte[] grpcCertificateHash,
            final byte[] rsaPublicKey) {
        this(nodeId, accountId, List.of(), "", gossipCaCertificate, grpcCertificateHash, rsaPublicKey);
    }

    /// X.509 DER bytes of the certificate that terminates mutual TLS for gossip
    ///
    /// Returns a copy of the `gossipCaCertificate` array.
    @Override
    public byte[] gossipCaCertificate() {
        return gossipCaCertificate.clone();
    }

    /// SHA-384 of the certificate served on serviceEndpoints; clients can pin against this; empty if the node has none
    ///
    /// Returns a copy of the `grpcCertificateHash` array.
    @Override
    public byte[] grpcCertificateHash() {
        return grpcCertificateHash.clone();
    }

    /// legacy gossip RSA public key (DER); only for tooling that still reads the legacy address-book format — prefer gossipCaCertificate and grpcCertificateHash
    ///
    /// Returns a copy of the `rsaPublicKey` array.
    @Override
    public byte[] rsaPublicKey() {
        return rsaPublicKey.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof NodeAddress other
            && nodeId == other.nodeId
            && Objects.equals(accountId, other.accountId)
            && Objects.equals(serviceEndpoints, other.serviceEndpoints)
            && Objects.equals(description, other.description)
            && Arrays.equals(gossipCaCertificate, other.gossipCaCertificate)
            && Arrays.equals(grpcCertificateHash, other.grpcCertificateHash)
            && Arrays.equals(rsaPublicKey, other.rsaPublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, accountId, serviceEndpoints, description, Arrays.hashCode(gossipCaCertificate), Arrays.hashCode(grpcCertificateHash), Arrays.hashCode(rsaPublicKey));
    }

    @Override
    public String toString() {
        return "NodeAddress[nodeId=" + nodeId + ", accountId=" + accountId + ", serviceEndpoints=" + serviceEndpoints + ", description=" + description + ", gossipCaCertificate=" + "byte[" + gossipCaCertificate.length + "]" + ", grpcCertificateHash=" + "byte[" + grpcCertificateHash.length + "]" + ", rsaPublicKey=" + "byte[" + rsaPublicKey.length + "]" + "]";
    }

    /// Returns a builder for a new `NodeAddress`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NodeAddress`.
    ///
    /// `build()` creates the `NodeAddress` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long nodeId;
        private @Nullable AccountId accountId;
        private List<ServiceEndpoint> serviceEndpoints = List.of();
        private String description = "";
        private byte @Nullable [] gossipCaCertificate;
        private byte @Nullable [] grpcCertificateHash;
        private byte @Nullable [] rsaPublicKey;

        private Builder() {
            // created by NodeAddress.builder()
        }

        /// Sets the `nodeId`.
        ///
        /// stable HIP-869 identifier; survives account / endpoint rotation
        ///
        /// @param nodeId the value
        /// @return this builder
        public Builder nodeId(final long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// fee account that receives this node's per-transaction share
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `serviceEndpoints`.
        ///
        /// public gRPC endpoints (clients submit transactions here)
        ///
        /// @param serviceEndpoints the value
        /// @return this builder
        public Builder serviceEndpoints(final List<ServiceEndpoint> serviceEndpoints) {
            this.serviceEndpoints = serviceEndpoints;
            return this;
        }

        /// Sets the `description`.
        ///
        /// free-form description of the node (at most 100 UTF-8 bytes); empty if none
        ///
        /// @param description the value
        /// @return this builder
        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        /// Sets the `gossipCaCertificate`.
        ///
        /// X.509 DER bytes of the certificate that terminates mutual TLS for gossip
        ///
        /// @param gossipCaCertificate the value
        /// @return this builder
        public Builder gossipCaCertificate(final byte[] gossipCaCertificate) {
            this.gossipCaCertificate = gossipCaCertificate;
            return this;
        }

        /// Sets the `grpcCertificateHash`.
        ///
        /// SHA-384 of the certificate served on serviceEndpoints; clients can pin against this; empty if the node has none
        ///
        /// @param grpcCertificateHash the value
        /// @return this builder
        public Builder grpcCertificateHash(final byte[] grpcCertificateHash) {
            this.grpcCertificateHash = grpcCertificateHash;
            return this;
        }

        /// Sets the `rsaPublicKey`.
        ///
        /// legacy gossip RSA public key (DER); only for tooling that still reads the legacy address-book format — prefer gossipCaCertificate and grpcCertificateHash
        ///
        /// @param rsaPublicKey the value
        /// @return this builder
        public Builder rsaPublicKey(final byte[] rsaPublicKey) {
            this.rsaPublicKey = rsaPublicKey;
            return this;
        }

        /// Creates the `NodeAddress` of the values set.
        ///
        /// @return the new `NodeAddress`
        /// @throws NullPointerException if a required value is not set
        public NodeAddress build() {
            return new NodeAddress(
                    Objects.requireNonNull(nodeId, "nodeId must not be null"),
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    serviceEndpoints,
                    description,
                    Objects.requireNonNull(gossipCaCertificate, "gossipCaCertificate must not be null"),
                    Objects.requireNonNull(grpcCertificateHash, "grpcCertificateHash must not be null"),
                    Objects.requireNonNull(rsaPublicKey, "rsaPublicKey must not be null"));
        }
    }
}
