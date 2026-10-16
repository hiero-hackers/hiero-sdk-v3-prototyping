package org.hiero.consensusnode.transactions;

import java.util.List;
import java.util.Objects;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.client.Submittable;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.client.internal.DefaultPackedTransaction;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// A packed, wire-ready transaction bound to a payer, a set of target nodes and a TransactionId. Its
/// content cannot change anymore; signatures can be added. Submitting it yields a Response.
/// Retry-tuning fields (maxAttempts, maxBackoff, minBackoff, attemptTimeout) and the
/// submit(client) method are inherited from Submittable.
public abstract class PackedTransaction<ReceiptT extends Receipt, TransactionT extends Transaction<ReceiptT, TransactionT>> extends Submittable<Response<ReceiptT>> {

    private final TransactionId transactionId;
    private final List<NodeSignature> nodeSignatures;

    /// Creates a new `PackedTransaction`.
    ///
    /// @throws NullPointerException if a required value is `null`
    protected PackedTransaction(final TransactionId transactionId, final List<NodeSignature> nodeSignatures) {
        super();
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        this.transactionId = transactionId;
        Objects.requireNonNull(nodeSignatures, "nodeSignatures must not be null");
        this.nodeSignatures = List.copyOf(nodeSignatures);
    }

    /// Returns the `transactionId`.
    public TransactionId transactionId() {
        return transactionId;
    }

    /// Returns the `nodeSignatures`.
    public List<NodeSignature> nodeSignatures() {
        return nodeSignatures;
    }

    /// Sets the `maxAttempts`.
    ///
    /// @param maxAttempts the new value
    /// @return this object
    @Override
    public PackedTransaction<ReceiptT, TransactionT> setMaxAttempts(final @Nullable Integer maxAttempts) {
        super.setMaxAttempts(maxAttempts);
        return this;
    }

    /// Sets the `maxBackoff`.
    ///
    /// @param maxBackoff the new value
    /// @return this object
    @Override
    public PackedTransaction<ReceiptT, TransactionT> setMaxBackoff(final @Nullable Long maxBackoff) {
        super.setMaxBackoff(maxBackoff);
        return this;
    }

    /// Sets the `minBackoff`.
    ///
    /// @param minBackoff the new value
    /// @return this object
    @Override
    public PackedTransaction<ReceiptT, TransactionT> setMinBackoff(final @Nullable Long minBackoff) {
        super.setMinBackoff(minBackoff);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public PackedTransaction<ReceiptT, TransactionT> setAttemptTimeout(final @Nullable Long attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Adds the signature of the account's private key for every target node. A key that has already signed
    /// this transaction is not added again.
    public abstract PackedTransaction<ReceiptT, TransactionT> sign(final Account account);

    /// Adds the signatures of the signer for every target node. A key that has already signed this transaction is
    /// not added again.
    public abstract PackedTransaction<ReceiptT, TransactionT> sign(final TransactionSigner signer);

    /// Returns the serialized TransactionBody bytes for every target node. Used by out-of-process
    /// signing flows (raw HSMs, async signing pipelines, multi-party coordination, audit archival)
    /// that cannot be wrapped behind a synchronous TransactionSigner. The returned list has one
    /// NodeBody per target node, in the order in which the nodes were given.
    public abstract List<NodeBody> signableBodies();

    /// Attaches externally-produced NodeSignatures to this PackedTransaction and returns a new
    /// PackedTransaction containing them. The provided list must contain one signature per node
    /// returned by signableBodies() for the same PublicKey; otherwise submit() will fail with
    /// INVALID_SIGNATURE on the chosen node.
    /// Throws if a signature references a node that is not a target node of this transaction, or if
    /// signatures for any target node are missing.
    public abstract PackedTransaction<ReceiptT, TransactionT> sign(final List<NodeSignature> signatures);

    public abstract byte[] toBytes();

    /// Loads a PackedTransaction from its serialized form (see toBytes()). The concrete transaction type is only known
    /// at runtime, so the result is typed with wildcards; callers check the concrete type themselves.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PackedTransaction<?, ?> fromBytes(final byte[] bytes) {
        return DefaultPackedTransaction.fromBytes(bytes);
    }
}
