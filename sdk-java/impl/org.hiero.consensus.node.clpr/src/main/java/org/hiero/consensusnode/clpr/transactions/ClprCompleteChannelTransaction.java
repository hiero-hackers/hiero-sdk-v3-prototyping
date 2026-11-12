package org.hiero.consensusnode.clpr.transactions;

import org.hiero.consensusnode.client.internal.Packing;
import org.hiero.consensusnode.clpr.internal.ClprKinds;
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
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Completes a Channel (phase 2 of 2): reveals the Channel ID and the key of the ownership commitment and activates the
/// Channel once the verifier contract has accepted the configuration of the peer ledger.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public final class ClprCompleteChannelTransaction extends Transaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> {

    private final ClprChannelOwnershipProof ownershipProof;
    private final ContractId verifierContract;
    private final byte[] configProof;
    private final byte[] endpointManifestProof;

    /// Creates a new `ClprCompleteChannelTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ClprCompleteChannelTransaction(
            final ClprChannelOwnershipProof ownershipProof,
            final ContractId verifierContract,
            final byte[] configProof,
            final byte[] endpointManifestProof) {
        super();
        Objects.requireNonNull(ownershipProof, "ownershipProof must not be null");
        this.ownershipProof = ownershipProof;
        Objects.requireNonNull(verifierContract, "verifierContract must not be null");
        this.verifierContract = verifierContract;
        Objects.requireNonNull(configProof, "configProof must not be null");
        this.configProof = configProof.clone();
        Objects.requireNonNull(endpointManifestProof, "endpointManifestProof must not be null");
        this.endpointManifestProof = endpointManifestProof.clone();
    }

    /// the Channel ID and the proof of the ownership of the commitment
    public ClprChannelOwnershipProof ownershipProof() {
        return ownershipProof;
    }

    /// The verifier contract that authenticates the peer ledger: a built-in verifier or a deployed contract. It cannot
    /// be changed for the lifetime of the Channel.
    public ContractId verifierContract() {
        return verifierContract;
    }

    /// The proof of the peer's ledger configuration, passed to the verifier: the configurationStateProof of the peer's
    /// ClprLedgerConfiguration, if the verifier accepts its encoding.
    public byte[] configProof() {
        return configProof.clone();
    }

    /// The proof of the peer's initial endpoint manifest: the manifestStateProof of the peer's ClprEndpointManifest, if
    /// the verifier accepts its encoding. Empty bytes are not a valid proof of an empty manifest for every verifier.
    public byte[] endpointManifestProof() {
        return endpointManifestProof.clone();
    }

    @Override
    public PackedTransaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ClprCompleteChannelReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ClprCompleteChannelReceipt, ClprCompleteChannelTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ClprKinds.COMPLETE_CHANNEL).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "ClprCompleteChannelTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", ownershipProof=" + ownershipProof()
                + ", verifierContract=" + verifierContract()
                + ", configProof=" + "byte[" + configProof().length + "]"
                + ", endpointManifestProof=" + "byte[" + endpointManifestProof().length + "]"
                + "]";
    }
}
