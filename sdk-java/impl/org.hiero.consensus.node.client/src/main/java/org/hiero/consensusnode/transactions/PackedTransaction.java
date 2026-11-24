package org.hiero.consensusnode.transactions;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.NodeBody;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.Submittable;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.client.internal.DefaultPackedTransaction;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// A packed, wire-ready transaction bound to a payer, a set of target nodes and a TransactionId. Its
/// content cannot change anymore; signatures can be added. Submitting it yields a Response.
/// The execution settings it can override (retryPolicy, attemptTimeout) and the submit(client) method are
/// inherited from Submittable.
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

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public PackedTransaction<ReceiptT, TransactionT> setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public PackedTransaction<ReceiptT, TransactionT> setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Adds the signatures of the signer for every target node, one signTransaction call each. A key that has already
    /// signed this transaction is not added again.
    public abstract CompletionStage<PackedTransaction<ReceiptT, TransactionT>> sign(final TransactionSigner signer);

    /// Returns the serialized TransactionBody bytes for every target node. Used by out-of-process
    /// signing flows (raw HSMs, async signing pipelines, multi-party coordination, audit archival)
    /// that cannot be wrapped behind a TransactionSigner. The returned list has one
    /// NodeBody per target node, in the order in which the nodes were given.
    public abstract List<NodeBody> signableBodies();

    /// Attaches externally-produced NodeSignatures to this PackedTransaction and returns a new
    /// PackedTransaction containing them; this PackedTransaction is not changed. The list must contain one signature
    /// per node returned by signableBodies() for each PublicKey. Fails with unknown-node-error if a signature references
    /// a node that is not a target node of this transaction, with incomplete-signatures-error if a public key has no
    /// signature for a target node, and with invalid-signature-error if a signature does not verify against the body of
    /// its node and its public key (checked here, so that a bad signature does not fail later as INVALID_SIGNATURE).
    ///
    /// @throws UnknownNodeException if an unknown node error occurs
    /// @throws IncompleteSignaturesException if an incomplete signatures error occurs
    /// @throws InvalidSignatureException if an invalid signature error occurs
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
