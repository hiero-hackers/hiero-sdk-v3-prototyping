package org.hiero.consensusnode.clpr.transactions;

import org.hiero.consensusnode.client.internal.Packing;
import org.hiero.consensusnode.clpr.internal.ClprKinds;
import org.hiero.consensusnode.clpr.internal.Ownership;
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
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Registers the ownership commitment of a new Channel (phase 1 of 2). Anybody may submit it. A commitment that is
/// already registered, also by a completed or closed Channel, fails with CLPR_CHANNEL_ALREADY_EXISTS.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public final class ClprRegisterChannelTransaction extends Transaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> {

    private final byte[] ownershipCommitment;

    /// Creates a new `ClprRegisterChannelTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public ClprRegisterChannelTransaction(final byte[] ownershipCommitment) {
        super();
        Objects.requireNonNull(ownershipCommitment, "ownershipCommitment must not be null");
        if (ownershipCommitment.length < 32) {
            throw new IllegalArgumentException("ownershipCommitment must contain at least 32 element(s)");
        }
        if (ownershipCommitment.length > 32) {
            throw new IllegalArgumentException("ownershipCommitment must contain at most 32 element(s)");
        }
        this.ownershipCommitment = ownershipCommitment.clone();
    }

    /// keccak256(channelId || publicKey)
    public byte[] ownershipCommitment() {
        return ownershipCommitment.clone();
    }

    @Override
    public PackedTransaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<ClprRegisterChannelReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<ClprRegisterChannelReceipt, ClprRegisterChannelTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(ClprKinds.REGISTER_CHANNEL).signForBatch(this, payerId, signer, batchKey);
    }

    /// Computes the ownership commitment keccak256(channelId || publicKey), with the public key in the encoding of the
    /// protocol (64 bytes x || y for secp256k1, 32 bytes for Ed25519).
    public static byte[] computeOwnershipCommitment(final byte[] channelId, final PublicKey publicKey) {
        return Ownership.channelCommitment(channelId, publicKey);
    }

    @Override
    public String toString() {
        return "ClprRegisterChannelTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", ownershipCommitment=" + "byte[" + ownershipCommitment().length + "]"
                + "]";
    }
}
