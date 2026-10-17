package org.hiero.consensusnode.transactions.topics;

import org.hiero.consensusnode.client.internal.TopicKinds;
import org.hiero.consensusnode.client.internal.Packing;
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
import org.hiero.ledger.Address;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Deletes a topic. Only possible if the topic has an adminAuthority (an immutable topic cannot be
/// deleted). The adminAuthority must sign.
public final class TopicDeleteTransaction extends Transaction<TopicDeleteReceipt, TopicDeleteTransaction> {

    private final Address topicId;

    /// Creates a new `TopicDeleteTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public TopicDeleteTransaction(final Address topicId) {
        super();
        Objects.requireNonNull(topicId, "topicId must not be null");
        this.topicId = topicId;
    }

    /// Returns the `topicId`.
    public Address topicId() {
        return topicId;
    }

    @Override
    public PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> pack(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).pack(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> signWithOperator(final HieroClient<?> client) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).signWithOperator(this, client);
    }

    @Override
    public PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> sign(final Account payer, final List<AccountId> nodes) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).sign(this, payer, nodes);
    }

    @Override
    public PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> sign(final AccountId payerId, final TransactionSigner signer, final List<AccountId> nodes) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).sign(this, payerId, signer, nodes);
    }

    @Override
    public CompletionStage<Response<TopicDeleteReceipt>> signWithOperatorAndSubmit(final HieroClient<?> client) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).signWithOperatorAndSubmit(this, client);
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
    public PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> packForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).packForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with the client's operator as payer and adds the operator's
    /// signature. The batch counterpart of signWithOperator(client).
    @Override
    public PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> signForBatchWithOperator(final HieroClient<?> client, final Authority batchKey) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).signForBatchWithOperator(this, client, batchKey);
    }

    /// Packs this transaction for a batch with the given Account as payer and adds its signature. The
    /// batch counterpart of sign(payer, nodes).
    @Override
    public PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> signForBatch(final Account payer, final Authority batchKey) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).signForBatch(this, payer, batchKey);
    }

    /// Packs this transaction for a batch with payerId as payer and signs it with the given
    /// TransactionSigner (HSM, hardware wallet, paymaster). The batch counterpart of
    /// sign(payerId, signer, nodes).
    @Override
    public PackedTransaction<TopicDeleteReceipt, TopicDeleteTransaction> signForBatch(final AccountId payerId, final TransactionSigner signer, final Authority batchKey) {
        return new Packing<>(TopicKinds.TOPIC_DELETE).signForBatch(this, payerId, signer, batchKey);
    }

    @Override
    public String toString() {
        return "TopicDeleteTransaction[maxTransactionFee=" + maxTransactionFee()
                + ", validDuration=" + validDuration()
                + ", memo=" + memo()
                + ", topicId=" + topicId()
                + "]";
    }
}
