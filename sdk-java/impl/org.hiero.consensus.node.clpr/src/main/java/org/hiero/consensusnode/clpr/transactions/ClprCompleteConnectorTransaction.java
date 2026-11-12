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

/// Completes a Connector (phase 2 of 2): reveals it, locks its stake and activates it. The Channel must exist; the
/// payer funds the locked stake.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public final class ClprCompleteConnectorTransaction extends Transaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> {

    private final ClprConnectorOwnershipProof ownershipProof;
    private final ContractId connectorContract;
    private final Authority adminAuthority;
    private final NativeToken<?, ?> lockedStake;

    /// Creates a new `ClprCompleteConnectorTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ClprCompleteConnectorTransaction(
            final ClprConnectorOwnershipProof ownershipProof,
            final ContractId connectorContract,
            final Authority adminAuthority,
            final NativeToken<?, ?> lockedStake) {
        super();
        Objects.requireNonNull(ownershipProof, "ownershipProof must not be null");
        this.ownershipProof = ownershipProof;
        Objects.requireNonNull(connectorContract, "connectorContract must not be null");
        this.connectorContract = connectorContract;
        Objects.requireNonNull(adminAuthority, "adminAuthority must not be null");
        this.adminAuthority = adminAuthority;
        Objects.requireNonNull(lockedStake, "lockedStake must not be null");
        this.lockedStake = lockedStake;
    }

    /// the Channel, the Connector ID and the proof of the ownership
    public ClprConnectorOwnershipProof ownershipProof() {
        return ownershipProof;
    }

    /// the deployed contract that authorizes the messages of the Connector
    public ContractId connectorContract() {
        return connectorContract;
    }

    /// required to deregister the Connector
    public Authority adminAuthority() {
        return adminAuthority;
    }

    /// The stake the network can slash; must reach the network's minimum (CLPR_INSUFFICIENT_STAKE otherwise).
    public NativeToken<?, ?> lockedStake() {
        return lockedStake;
    }

    @Override
    public PackedTransaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ClprCompleteConnectorReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ClprCompleteConnectorReceipt, ClprCompleteConnectorTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ClprKinds.COMPLETE_CONNECTOR).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "ClprCompleteConnectorTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", ownershipProof=" + ownershipProof()
                + ", connectorContract=" + connectorContract()
                + ", adminAuthority=" + adminAuthority()
                + ", lockedStake=" + lockedStake()
                + "]";
    }
}
