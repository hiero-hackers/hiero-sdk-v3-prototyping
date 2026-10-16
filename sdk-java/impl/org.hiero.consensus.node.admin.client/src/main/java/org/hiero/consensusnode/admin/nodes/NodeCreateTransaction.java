package org.hiero.consensusnode.admin.nodes;

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

/// Registers a new consensus node in the address book. All endpoint, certificate, and key
/// fields are required; the network has no defaults for any of them.
public final class NodeCreateTransaction extends Transaction<NodeCreateReceipt, NodeCreateTransaction> {

    private final AccountId accountId;
    private final @Nullable String description;
    private final List<ServiceEndpoint> gossipEndpoints;
    private final List<ServiceEndpoint> serviceEndpoints;
    private final byte[] gossipCaCertificate;
    private final byte[] grpcCertificateHash;
    private final Authority adminAuthority;
    private final boolean declineReward;
    private final @Nullable ServiceEndpoint grpcWebProxyEndpoint;

    /// Creates a new `NodeCreateTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public NodeCreateTransaction(
            final AccountId accountId,
            final @Nullable String description,
            final List<ServiceEndpoint> gossipEndpoints,
            final List<ServiceEndpoint> serviceEndpoints,
            final byte[] gossipCaCertificate,
            final byte[] grpcCertificateHash,
            final Authority adminAuthority,
            final boolean declineReward,
            final @Nullable ServiceEndpoint grpcWebProxyEndpoint) {
        super();
        Objects.requireNonNull(accountId, "accountId must not be null");
        this.accountId = accountId;
        this.description = description;
        Objects.requireNonNull(gossipEndpoints, "gossipEndpoints must not be null");
        if (gossipEndpoints.size() < 1) {
            throw new IllegalArgumentException("gossipEndpoints must contain at least 1 element(s)");
        }
        if (gossipEndpoints.size() > 10) {
            throw new IllegalArgumentException("gossipEndpoints must contain at most 10 element(s)");
        }
        this.gossipEndpoints = List.copyOf(gossipEndpoints);
        Objects.requireNonNull(serviceEndpoints, "serviceEndpoints must not be null");
        if (serviceEndpoints.size() < 1) {
            throw new IllegalArgumentException("serviceEndpoints must contain at least 1 element(s)");
        }
        if (serviceEndpoints.size() > 8) {
            throw new IllegalArgumentException("serviceEndpoints must contain at most 8 element(s)");
        }
        this.serviceEndpoints = List.copyOf(serviceEndpoints);
        Objects.requireNonNull(gossipCaCertificate, "gossipCaCertificate must not be null");
        this.gossipCaCertificate = gossipCaCertificate.clone();
        Objects.requireNonNull(grpcCertificateHash, "grpcCertificateHash must not be null");
        this.grpcCertificateHash = grpcCertificateHash.clone();
        Objects.requireNonNull(adminAuthority, "adminAuthority must not be null");
        this.adminAuthority = adminAuthority;
        this.declineReward = declineReward;
        this.grpcWebProxyEndpoint = grpcWebProxyEndpoint;
    }

    /// Creates a new `NodeCreateTransaction` with the default value of `declineReward`.
    public NodeCreateTransaction(
            final AccountId accountId,
            final @Nullable String description,
            final List<ServiceEndpoint> gossipEndpoints,
            final List<ServiceEndpoint> serviceEndpoints,
            final byte[] gossipCaCertificate,
            final byte[] grpcCertificateHash,
            final Authority adminAuthority,
            final @Nullable ServiceEndpoint grpcWebProxyEndpoint) {
        this(accountId, description, gossipEndpoints, serviceEndpoints, gossipCaCertificate, grpcCertificateHash, adminAuthority, false, grpcWebProxyEndpoint);
    }

    /// account that receives the node's staking rewards
    public AccountId accountId() {
        return accountId;
    }

    /// free-form description (at most 100 characters)
    public @Nullable String description() {
        return description;
    }

    /// inter-node hashgraph gossip endpoints
    public List<ServiceEndpoint> gossipEndpoints() {
        return gossipEndpoints;
    }

    /// public gRPC endpoints for client transactions
    public List<ServiceEndpoint> serviceEndpoints() {
        return serviceEndpoints;
    }

    /// X.509 DER bytes of the certificate that terminates mutual TLS for gossip
    public byte[] gossipCaCertificate() {
        return gossipCaCertificate.clone();
    }

    /// SHA-384 of the certificate served on serviceEndpoints; clients pin against this
    public byte[] grpcCertificateHash() {
        return grpcCertificateHash.clone();
    }

    /// required to update / delete the node; must co-sign this create
    public Authority adminAuthority() {
        return adminAuthority;
    }

    /// true → node opts out of staking rewards
    public boolean declineReward() {
        return declineReward;
    }

    /// optional gRPC-web proxy endpoint (HIP-1046)
    public @Nullable ServiceEndpoint grpcWebProxyEndpoint() {
        return grpcWebProxyEndpoint;
    }

    @Override
    public PackedTransaction<NodeCreateReceipt, NodeCreateTransaction> pack(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.pack");
    }

    @Override
    public PackedTransaction<NodeCreateReceipt, NodeCreateTransaction> signWithOperator(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.signWithOperator");
    }

    @Override
    public PackedTransaction<NodeCreateReceipt, NodeCreateTransaction> sign(final Account payer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.sign");
    }

    @Override
    public PackedTransaction<NodeCreateReceipt, NodeCreateTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.sign");
    }

    @Override
    public CompletionStage<Response<NodeCreateReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.signWithOperatorAndSubmit");
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
    public PackedTransaction<NodeCreateReceipt, NodeCreateTransaction> packForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.packForBatch");
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<NodeCreateReceipt, NodeCreateTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.signForBatchWithOperator");
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<NodeCreateReceipt, NodeCreateTransaction> signForBatch(final Account payer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.signForBatch");
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<NodeCreateReceipt, NodeCreateTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        throw new UnsupportedOperationException("Not implemented yet: NodeCreateTransaction.signForBatch");
    }

    /// Returns a builder for a new `NodeCreateTransaction`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `NodeCreateTransaction`.
    ///
    /// `build()` creates the `NodeCreateTransaction` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable AccountId accountId;
        private @Nullable String description;
        private @Nullable List<ServiceEndpoint> gossipEndpoints;
        private @Nullable List<ServiceEndpoint> serviceEndpoints;
        private byte @Nullable [] gossipCaCertificate;
        private byte @Nullable [] grpcCertificateHash;
        private @Nullable Authority adminAuthority;
        private boolean declineReward = false;
        private @Nullable ServiceEndpoint grpcWebProxyEndpoint;

        private Builder() {
            // created by NodeCreateTransaction.builder()
        }

        /// Sets the `accountId`.
        ///
        /// account that receives the node's staking rewards
        ///
        /// @param accountId the value
        /// @return this builder
        public Builder accountId(final AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        /// Sets the `description`.
        ///
        /// free-form description (at most 100 characters)
        ///
        /// @param description the value
        /// @return this builder
        public Builder description(final @Nullable String description) {
            this.description = description;
            return this;
        }

        /// Sets the `gossipEndpoints`.
        ///
        /// inter-node hashgraph gossip endpoints
        ///
        /// @param gossipEndpoints the value
        /// @return this builder
        public Builder gossipEndpoints(final List<ServiceEndpoint> gossipEndpoints) {
            this.gossipEndpoints = gossipEndpoints;
            return this;
        }

        /// Sets the `serviceEndpoints`.
        ///
        /// public gRPC endpoints for client transactions
        ///
        /// @param serviceEndpoints the value
        /// @return this builder
        public Builder serviceEndpoints(final List<ServiceEndpoint> serviceEndpoints) {
            this.serviceEndpoints = serviceEndpoints;
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
        /// SHA-384 of the certificate served on serviceEndpoints; clients pin against this
        ///
        /// @param grpcCertificateHash the value
        /// @return this builder
        public Builder grpcCertificateHash(final byte[] grpcCertificateHash) {
            this.grpcCertificateHash = grpcCertificateHash;
            return this;
        }

        /// Sets the `adminAuthority`.
        ///
        /// required to update / delete the node; must co-sign this create
        ///
        /// @param adminAuthority the value
        /// @return this builder
        public Builder adminAuthority(final Authority adminAuthority) {
            this.adminAuthority = adminAuthority;
            return this;
        }

        /// Sets the `declineReward`.
        ///
        /// true → node opts out of staking rewards
        ///
        /// @param declineReward the value
        /// @return this builder
        public Builder declineReward(final boolean declineReward) {
            this.declineReward = declineReward;
            return this;
        }

        /// Sets the `grpcWebProxyEndpoint`.
        ///
        /// optional gRPC-web proxy endpoint (HIP-1046)
        ///
        /// @param grpcWebProxyEndpoint the value
        /// @return this builder
        public Builder grpcWebProxyEndpoint(final @Nullable ServiceEndpoint grpcWebProxyEndpoint) {
            this.grpcWebProxyEndpoint = grpcWebProxyEndpoint;
            return this;
        }

        /// Creates the `NodeCreateTransaction` of the values set.
        ///
        /// @return the new `NodeCreateTransaction`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public NodeCreateTransaction build() {
            return new NodeCreateTransaction(
                    Objects.requireNonNull(accountId, "accountId must not be null"),
                    description,
                    Objects.requireNonNull(gossipEndpoints, "gossipEndpoints must not be null"),
                    Objects.requireNonNull(serviceEndpoints, "serviceEndpoints must not be null"),
                    Objects.requireNonNull(gossipCaCertificate, "gossipCaCertificate must not be null"),
                    Objects.requireNonNull(grpcCertificateHash, "grpcCertificateHash must not be null"),
                    Objects.requireNonNull(adminAuthority, "adminAuthority must not be null"),
                    declineReward,
                    grpcWebProxyEndpoint);
        }
    }

    @Override
    public String toString() {
        return "NodeCreateTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", accountId=" + accountId()
                + ", description=" + description()
                + ", gossipEndpoints=" + gossipEndpoints()
                + ", serviceEndpoints=" + serviceEndpoints()
                + ", gossipCaCertificate=" + "byte[" + gossipCaCertificate().length + "]"
                + ", grpcCertificateHash=" + "byte[" + grpcCertificateHash().length + "]"
                + ", adminAuthority=" + adminAuthority()
                + ", declineReward=" + declineReward()
                + ", grpcWebProxyEndpoint=" + grpcWebProxyEndpoint()
                + "]";
    }
}
