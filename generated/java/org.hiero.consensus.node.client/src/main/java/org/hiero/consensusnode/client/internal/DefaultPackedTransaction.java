package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.NodeBody;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.hapi.proto.SignatureMap;
import org.hiero.hapi.proto.SignedTransaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.TransactionId;

/// A packed transaction: the serialized bodies per target node plus the signatures collected so far.
///
/// @param <ReceiptT>     the receipt of the transaction
/// @param <TransactionT> the transaction type
public final class DefaultPackedTransaction<ReceiptT extends Receipt,
        TransactionT extends Transaction<ReceiptT, TransactionT>>
        extends PackedTransaction<ReceiptT, TransactionT> {

    private final List<NodeBody> bodies;
    private final ConsensusNode node;
    private final String service;
    private final String method;
    private final ClientRuntime.ReceiptFactory receipts;

    /// Creates a packed transaction.
    ///
    /// @param transactionId  the transaction id
    /// @param nodeSignatures the signatures collected so far
    /// @param bodies         the serialized body per target node
    /// @param node           the node the transaction is sent to
    /// @param service        the qualified name of the service that accepts it
    /// @param method         the name of the method that accepts it
    /// @param receipts       builds the typed receipt from the protobuf receipt
    public DefaultPackedTransaction(final TransactionId transactionId, final List<NodeSignature> nodeSignatures,
                                    final List<NodeBody> bodies, final ConsensusNode node,
                                    final String service, final String method,
                                    final ClientRuntime.ReceiptFactory receipts) {
        super(transactionId, nodeSignatures);
        this.bodies = List.copyOf(bodies);
        this.node = Objects.requireNonNull(node, "node must not be null");
        this.service = Objects.requireNonNull(service, "service must not be null");
        this.method = Objects.requireNonNull(method, "method must not be null");
        this.receipts = Objects.requireNonNull(receipts, "receipts must not be null");
    }

    @Override
    public List<NodeBody> signableBodies() {
        return bodies;
    }

    @Override
    public PackedTransaction<ReceiptT, TransactionT> sign(final Account account) {
        Objects.requireNonNull(account, "account must not be null");
        return sign((bytes, target) -> new NodeSignature(target,
                account.privateKey().createPublicKey(), account.privateKey().sign(bytes)));
    }

    @Override
    public PackedTransaction<ReceiptT, TransactionT> sign(final TransactionSigner signer) {
        Objects.requireNonNull(signer, "signer must not be null");
        final List<NodeSignature> signatures = new ArrayList<>();
        for (final NodeBody body : bodies) {
            signatures.add(signer.signTransaction(body.bytes(), body.node()));
        }
        return sign(signatures);
    }

    @Override
    public PackedTransaction<ReceiptT, TransactionT> sign(final List<NodeSignature> signatures) {
        Objects.requireNonNull(signatures, "signatures must not be null");
        final List<NodeSignature> all = new ArrayList<>(nodeSignatures());
        all.addAll(signatures);
        return new DefaultPackedTransaction<>(transactionId(), all, bodies, node, service, method, receipts);
    }

    @Override
    public byte[] toBytes() {
        return protoTransaction(node.account()).toByteArray();
    }

    @Override
    public CompletionStage<Response<ReceiptT>> submit(final HieroClient<?> client) {
        Objects.requireNonNull(client, "client must not be null");
        final TransactionId id = transactionId();
        return ClientRuntime.of(client)
                .submit(node, service, method, protoTransaction(node.account()), id, receipts)
                .thenApply(accepted -> new Response<ReceiptT>(id));
    }

    private org.hiero.hapi.proto.Transaction protoTransaction(final AccountId target) {
        final NodeBody body = bodies.stream()
                .filter(candidate -> candidate.node().equals(target))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("The transaction was not packed for node " + target));
        final SignatureMap.Builder signatures = SignatureMap.newBuilder();
        nodeSignatures().stream()
                .filter(signature -> signature.node().equals(target))
                .forEach(signature -> signatures.addSigPair(
                        Protobuf.toSignaturePair(signature.publicKey(), signature.signature())));
        final SignedTransaction signed = SignedTransaction.newBuilder()
                .setBodyBytes(ByteString.copyFrom(body.bytes()))
                .setSigMap(signatures)
                .build();
        return org.hiero.hapi.proto.Transaction.newBuilder()
                .setSignedTransactionBytes(signed.toByteString())
                .build();
    }
}
