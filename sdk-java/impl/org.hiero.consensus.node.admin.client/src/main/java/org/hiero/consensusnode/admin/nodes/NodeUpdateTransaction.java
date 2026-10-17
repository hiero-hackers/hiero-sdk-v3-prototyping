package org.hiero.consensusnode.admin.nodes;

import org.hiero.consensusnode.admin.internal.AdminKinds;
import org.hiero.consensusnode.client.internal.Packing;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Updates one or more of a node's mutable fields. Fields that are not set are left unchanged. Setting an endpoint
/// list replaces the entire current list — there is no way to add or remove a single entry, and no way to clear a
/// list: an empty list means "leave this list as it is".
public final class NodeUpdateTransaction extends Transaction<NodeUpdateReceipt, NodeUpdateTransaction> {

    private final long nodeId;
    private final @Nullable AccountId accountId;
    private final @Nullable String description;
    private final List<ServiceEndpoint> gossipEndpoints;
    private final List<ServiceEndpoint> serviceEndpoints;
    private final byte @Nullable [] gossipCaCertificate;
    private final byte @Nullable [] grpcCertificateHash;
    private final @Nullable Authority adminAuthority;
    private final @Nullable Boolean declineReward;
    private final @Nullable ServiceEndpoint grpcWebProxyEndpoint;

    /// Creates a new `NodeUpdateTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public NodeUpdateTransaction(
            final long nodeId,
            final @Nullable AccountId accountId,
            final @Nullable String description,
            final List<ServiceEndpoint> gossipEndpoints,
            final List<ServiceEndpoint> serviceEndpoints,
            final byte @Nullable [] gossipCaCertificate,
            final byte @Nullable [] grpcCertificateHash,
            final @Nullable Authority adminAuthority,
            final @Nullable Boolean declineReward,
            final @Nullable ServiceEndpoint grpcWebProxyEndpoint) {
        super();
        this.nodeId = nodeId;
        this.accountId = accountId;
        if (description != null && description.getBytes(StandardCharsets.UTF_8).length > 100) {
            throw new IllegalArgumentException("description must be at most 100 bytes long in UTF-8");
        }
        this.description = description;
        Objects.requireNonNull(gossipEndpoints, "gossipEndpoints must not be null");
        if (gossipEndpoints.size() > 10) {
            throw new IllegalArgumentException("gossipEndpoints must contain at most 10 element(s)");
        }
        this.gossipEndpoints = List.copyOf(gossipEndpoints);
        Objects.requireNonNull(serviceEndpoints, "serviceEndpoints must not be null");
        if (serviceEndpoints.size() > 8) {
            throw new IllegalArgumentException("serviceEndpoints must contain at most 8 element(s)");
        }
        this.serviceEndpoints = List.copyOf(serviceEndpoints);
        this.gossipCaCertificate = gossipCaCertificate == null ? null : gossipCaCertificate.clone();
        this.grpcCertificateHash = grpcCertificateHash == null ? null : grpcCertificateHash.clone();
        this.adminAuthority = adminAuthority;
        this.declineReward = declineReward;
        this.grpcWebProxyEndpoint = grpcWebProxyEndpoint;
    }

    /// Creates a new `NodeUpdateTransaction` with the default value of `gossipEndpoints`, `serviceEndpoints`.
    public NodeUpdateTransaction(
            final long nodeId,
            final @Nullable AccountId accountId,
            final @Nullable String description,
            final byte @Nullable [] gossipCaCertificate,
            final byte @Nullable [] grpcCertificateHash,
            final @Nullable Authority adminAuthority,
            final @Nullable Boolean declineReward,
            final @Nullable ServiceEndpoint grpcWebProxyEndpoint) {
        this(nodeId, accountId, description, List.of(), List.of(), gossipCaCertificate, grpcCertificateHash, adminAuthority, declineReward, grpcWebProxyEndpoint);
    }

    /// the node to update
    public long nodeId() {
        return nodeId;
    }

    /// when set, the new account's key must also sign
    public @Nullable AccountId accountId() {
        return accountId;
    }

    /// new free-form description
    public @Nullable String description() {
        return description;
    }

    /// when not empty, replaces the entire list; empty leaves it unchanged
    public List<ServiceEndpoint> gossipEndpoints() {
        return gossipEndpoints;
    }

    /// when not empty, replaces the entire list; empty leaves it unchanged
    public List<ServiceEndpoint> serviceEndpoints() {
        return serviceEndpoints;
    }

    /// new gossip CA certificate (X.509 DER bytes)
    public byte @Nullable [] gossipCaCertificate() {
        return gossipCaCertificate == null ? null : gossipCaCertificate.clone();
    }

    /// new SHA-384 hash of the gRPC certificate
    public byte @Nullable [] grpcCertificateHash() {
        return grpcCertificateHash == null ? null : grpcCertificateHash.clone();
    }

    /// when set, the new key must also sign
    public @Nullable Authority adminAuthority() {
        return adminAuthority;
    }

    /// whether the node opts out of staking rewards
    public @Nullable Boolean declineReward() {
        return declineReward;
    }

    /// new gRPC-web proxy endpoint (HIP-1046)
    public @Nullable ServiceEndpoint grpcWebProxyEndpoint() {
        return grpcWebProxyEndpoint;
    }

    @Override
    public PackedTransaction<NodeUpdateReceipt, NodeUpdateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(AdminKinds.NODE_UPDATE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<NodeUpdateReceipt, NodeUpdateTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(AdminKinds.NODE_UPDATE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<NodeUpdateReceipt, NodeUpdateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(AdminKinds.NODE_UPDATE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<NodeUpdateReceipt, NodeUpdateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(AdminKinds.NODE_UPDATE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<NodeUpdateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(AdminKinds.NODE_UPDATE).signWithOperatorAndSubmit(this, client);
    }

    /// Packs this transaction as the *inner* transaction of a BatchTransaction (HIP-551), without
    /// signing it. Unlike pack(payer, nodes), this produces a single TransactionBody addressed to no
    /// consensus node (nodeAccountID = 0.0.0) instead of one body per target node — an inner batch
    /// transaction is never submitted to a node on its own; it is embedded into
    /// BatchTransaction.innerTransactions and executed by the network as part of the batch. The
    /// returned PackedTransaction still carries its own TransactionId (payer + validStart); further
    /// signatures use PackedTransaction.sign(...) / signableBodies().
    ///
    /// batchKey names the Authority that must sign the *outer* BatchTransaction for this inner
    /// transaction to execute — the inner author's controlled opt-in to being batched. Whether a
    /// transaction type may be batched at all is decided by the network.
    @Override
    public PackedTransaction<NodeUpdateReceipt, NodeUpdateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(AdminKinds.NODE_UPDATE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<NodeUpdateReceipt, NodeUpdateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(AdminKinds.NODE_UPDATE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<NodeUpdateReceipt, NodeUpdateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(AdminKinds.NODE_UPDATE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<NodeUpdateReceipt, NodeUpdateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(AdminKinds.NODE_UPDATE).signForBatch(this, payerId, signer, batchKey);
    }

    /// Returns a builder for a new `NodeUpdateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NodeUpdateTransaction`.
    ///
    /// `build()` creates the `NodeUpdateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable Long nodeId;
        private @Nullable AccountId accountId;
        private @Nullable String description;
        private List<ServiceEndpoint> gossipEndpoints = List.of();
        private List<ServiceEndpoint> serviceEndpoints = List.of();
        private byte @Nullable [] gossipCaCertificate;
        private byte @Nullable [] grpcCertificateHash;
        private @Nullable Authority adminAuthority;
        private @Nullable Boolean declineReward;
        private @Nullable ServiceEndpoint grpcWebProxyEndpoint;

        private Builder() {
            // created by NodeUpdateTransaction.builder()
        }

        /// Sets the `nodeId`.
        ///
        /// the node to update
        ///
        /// @param nodeId the value
        /// @return this builder
        public Builder nodeId(final long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /// Sets the `accountId`.
        ///
        /// when set, the new account's key must also sign
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final @Nullable AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `description`.
        ///
        /// new free-form description
        ///
        /// @param description the value
        /// @return this builder
        public Builder description(final @Nullable String description) {
            this.description = description;
            return this;
        }

        /// Sets the `gossipEndpoints`.
        ///
        /// when not empty, replaces the entire list; empty leaves it unchanged
        ///
        /// @param gossipEndpoints the value
        /// @return this builder
        public Builder gossipEndpoints(final List<ServiceEndpoint> gossipEndpoints) {
            this.gossipEndpoints = gossipEndpoints;
            return this;
        }

        /// Sets the `serviceEndpoints`.
        ///
        /// when not empty, replaces the entire list; empty leaves it unchanged
        ///
        /// @param serviceEndpoints the value
        /// @return this builder
        public Builder serviceEndpoints(final List<ServiceEndpoint> serviceEndpoints) {
            this.serviceEndpoints = serviceEndpoints;
            return this;
        }

        /// Sets the `gossipCaCertificate`.
        ///
        /// new gossip CA certificate (X.509 DER bytes)
        ///
        /// @param gossipCaCertificate the value
        /// @return this builder
        public Builder gossipCaCertificate(final byte @Nullable [] gossipCaCertificate) {
            this.gossipCaCertificate = gossipCaCertificate;
            return this;
        }

        /// Sets the `grpcCertificateHash`.
        ///
        /// new SHA-384 hash of the gRPC certificate
        ///
        /// @param grpcCertificateHash the value
        /// @return this builder
        public Builder grpcCertificateHash(final byte @Nullable [] grpcCertificateHash) {
            this.grpcCertificateHash = grpcCertificateHash;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// when set, the new key must also sign
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final @Nullable Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `declineReward`.
        ///
        /// whether the node opts out of staking rewards
        ///
        /// @param declineReward the value
        /// @return this builder
        public Builder declineReward(final @Nullable Boolean declineReward) {
            this.declineReward = declineReward;
            return this;
        }

        /// Sets the `grpcWebProxyEndpoint`.
        ///
        /// new gRPC-web proxy endpoint (HIP-1046)
        ///
        /// @param grpcWebProxyEndpoint the value
        /// @return this builder
        public Builder grpcWebProxyEndpoint(final @Nullable ServiceEndpoint grpcWebProxyEndpoint) {
            this.grpcWebProxyEndpoint = grpcWebProxyEndpoint;
            return this;
        }

        /// Creates the `NodeUpdateTransaction` of the values set.
        ///
        /// @return the new `NodeUpdateTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public NodeUpdateTransaction build() {
            return new NodeUpdateTransaction(
                    Objects.requireNonNull(nodeId, "nodeId must not be null"),
                    accountId,
                    description,
                    gossipEndpoints,
                    serviceEndpoints,
                    gossipCaCertificate,
                    grpcCertificateHash,
                    adminAuthority,
                    declineReward,
                    grpcWebProxyEndpoint);
        }
    }

    @Override
    public String toString() {
        return "NodeUpdateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", nodeId=" + nodeId()
                + ", accountId=" + accountId()
                + ", description=" + description()
                + ", gossipEndpoints=" + gossipEndpoints()
                + ", serviceEndpoints=" + serviceEndpoints()
                + ", gossipCaCertificate=" + (gossipCaCertificate() == null ? "null" : "byte[" + gossipCaCertificate().length + "]")
                + ", grpcCertificateHash=" + (grpcCertificateHash() == null ? "null" : "byte[" + grpcCertificateHash().length + "]")
                + ", adminAuthority=" + adminAuthority()
                + ", declineReward=" + declineReward()
                + ", grpcWebProxyEndpoint=" + grpcWebProxyEndpoint()
                + "]";
    }
}
