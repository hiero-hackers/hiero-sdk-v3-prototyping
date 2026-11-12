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
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Delivers a bundle of messages of the peer ledger of a Channel, with the proof that authenticates them.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public final class ClprSubmitBundleTransaction extends Transaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> {

    private final byte[] channelId;
    private final byte[] bundlePayload;

    /// Creates a new `ClprSubmitBundleTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ClprSubmitBundleTransaction(final byte[] channelId, final byte[] bundlePayload) {
        super();
        Objects.requireNonNull(channelId, "channelId must not be null");
        if (channelId.length < 32) {
            throw new IllegalArgumentException("channelId must contain at least 32 element(s)");
        }
        if (channelId.length > 32) {
            throw new IllegalArgumentException("channelId must contain at most 32 element(s)");
        }
        this.channelId = channelId.clone();
        Objects.requireNonNull(bundlePayload, "bundlePayload must not be null");
        this.bundlePayload = bundlePayload.clone();
    }

    /// the Channel the bundle belongs to
    public byte[] channelId() {
        return channelId.clone();
    }

    /// the bundle; interpreted only by the verifier contract of the Channel
    public byte[] bundlePayload() {
        return bundlePayload.clone();
    }

    @Override
    public PackedTransaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ClprSubmitBundleReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ClprSubmitBundleReceipt, ClprSubmitBundleTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ClprKinds.SUBMIT_BUNDLE).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "ClprSubmitBundleTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", channelId=" + "byte[" + channelId().length + "]"
                + ", bundlePayload=" + "byte[" + bundlePayload().length + "]"
                + "]";
    }
}
