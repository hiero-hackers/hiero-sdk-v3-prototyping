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
import org.hiero.consensusnode.clpr.ClprThrottles;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Replaces the CLPR limits of this ledger. Only the network's treasury or system admin account may submit it.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public final class ClprUpdateLedgerConfigurationTransaction extends Transaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> {

    private final ClprThrottles throttles;

    /// Creates a new `ClprUpdateLedgerConfigurationTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ClprUpdateLedgerConfigurationTransaction(final ClprThrottles throttles) {
        super();
        Objects.requireNonNull(throttles, "throttles must not be null");
        this.throttles = throttles;
    }

    /// the new limits; replace all current ones
    public ClprThrottles throttles() {
        return throttles;
    }

    @Override
    public PackedTransaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ClprUpdateLedgerConfigurationReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ClprUpdateLedgerConfigurationReceipt, ClprUpdateLedgerConfigurationTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ClprKinds.UPDATE_LEDGER_CONFIGURATION).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "ClprUpdateLedgerConfigurationTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", throttles=" + throttles()
                + "]";
    }
}
