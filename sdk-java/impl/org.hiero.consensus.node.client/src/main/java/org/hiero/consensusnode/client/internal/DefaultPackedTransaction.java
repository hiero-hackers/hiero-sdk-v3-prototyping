package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TransactionList;
import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignaturePair;
import com.hederahashgraph.api.proto.java.SignedTransaction;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.NodeBody;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.consensusnode.transactions.IncompleteSignaturesException;
import org.hiero.consensusnode.transactions.InvalidSignatureException;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.consensusnode.transactions.UnknownNodeException;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.TransactionId;
import org.jspecify.annotations.Nullable;

/// The only concrete `PackedTransaction`: one serialized `TransactionBody` per target node and the signatures over
/// them. Its byte form is the `TransactionList` all Hiero SDKs write - one `Transaction` with a `SignedTransaction`
/// per node.
///
/// @param <R> the receipt type
/// @param <T> the transaction type
public final class DefaultPackedTransaction<R extends Receipt, T extends Transaction<R, T>>
        extends PackedTransaction<R, T> {

    private final List<NodeBody> bodies;
    private final @Nullable TransactionKind<R, T> kind;

    /// Creates a packed transaction.
    ///
    /// @param transactionId  the id
    /// @param nodeSignatures the signatures
    /// @param bodies         the body of every target node, in the order of the nodes
    /// @param kind           the transaction type, `null` if it is not implemented (it cannot be submitted then)
    public DefaultPackedTransaction(final TransactionId transactionId, final List<NodeSignature> nodeSignatures,
                                    final List<NodeBody> bodies, final @Nullable TransactionKind<R, T> kind) {
        super(transactionId, nodeSignatures);
        this.bodies = List.copyOf(bodies);
        this.kind = kind;
    }

    @Override
    public List<NodeBody> signableBodies() {
        return bodies;
    }

    @Override
    public PackedTransaction<R, T> sign(final Account account) {
        final PublicKey publicKey = account.privateKey().createPublicKey();
        return withSignatures(bodies.stream()
                .map(body -> new NodeSignature(body.node(), publicKey, account.privateKey().sign(body.bytes())))
                .toList());
    }

    @Override
    public CompletionStage<PackedTransaction<R, T>> sign(final TransactionSigner signer) {
        Objects.requireNonNull(signer, "signer must not be null");
        // rule signer-concurrency: the bodies of all target nodes are handed to the signer at once
        final List<CompletableFuture<NodeSignature>> pending = bodies.stream()
                .map(body -> signer.signTransaction(body).toCompletableFuture()).toList();
        return CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new)).thenApply(done -> {
            final List<NodeSignature> signatures = new ArrayList<>();
            for (int i = 0; i < bodies.size(); i++) {
                final NodeSignature signature = pending.get(i).join();
                // rule signer-identity: the signature is the one of the body's node and of the signer's key
                if (!signature.node().equals(bodies.get(i).node())
                        || !signature.publicKey().equals(signer.publicKey())) {
                    throw new IllegalStateException("The signer signed the body of " + bodies.get(i).node()
                            + " for " + signature.node() + " with another key than its public key");
                }
                signatures.add(signature);
            }
            return withSignatures(signatures);
        });
    }

    @Override
    public PackedTransaction<R, T> sign(final List<NodeSignature> signatures) {
        final Map<AccountId, NodeBody> byNode = bodies.stream()
                .collect(Collectors.toMap(NodeBody::node, Function.identity()));
        for (final NodeSignature signature : signatures) {
            if (!byNode.containsKey(signature.node())) {
                throw new UnknownNodeException("The signature references " + signature.node()
                        + ", which is no target node of the transaction", null);
            }
        }
        final Map<PublicKey, Set<AccountId>> signedNodes = signatures.stream().collect(Collectors.groupingBy(
                NodeSignature::publicKey, Collectors.mapping(NodeSignature::node, Collectors.toSet())));
        signedNodes.forEach((key, signed) -> {
            if (!signed.containsAll(byNode.keySet())) {
                throw new IncompleteSignaturesException("The signatures of a key do not cover every target node",
                        null);
            }
        });
        for (final NodeSignature signature : signatures) {
            if (!signature.publicKey().verify(byNode.get(signature.node()).bytes(), signature.signature())) {
                throw new InvalidSignatureException("The signature of " + signature.publicKey() + " for "
                        + signature.node() + " does not verify against the body of the node", null);
            }
        }
        return withSignatures(signatures);
    }

    /// This transaction with the given signatures added; a signature of a key that has already signed the body of
    /// its node is not added again.
    private PackedTransaction<R, T> withSignatures(final List<NodeSignature> signatures) {
        final List<NodeSignature> all = new ArrayList<>(nodeSignatures());
        for (final NodeSignature signature : signatures) {
            if (!hasSigned(all, signature.node(), signature.publicKey())) {
                all.add(signature);
            }
        }
        return new DefaultPackedTransaction<>(transactionId(), all, bodies, kind);
    }

    @Override
    public byte[] toBytes() {
        final TransactionList.Builder list = TransactionList.newBuilder();
        bodies.forEach(body -> list.addTransactionList(transaction(body)));
        return list.build().toByteArray();
    }

    @Override
    public CompletionStage<Response<R>> submit(final HieroClient<?> client) {
        if (kind == null) {
            return CompletableFuture.failedFuture(new IllegalStateException(
                    "The transaction has a body no registered transaction support knows; it cannot be submitted"));
        }
        final TransactionKind<R, T> type = kind;
        final ClientRuntime runtime = ClientRuntime.of(client);
        // rule candidates-of-a-packed-transaction: a packed transaction goes only to the nodes it was packed for
        final List<ConsensusNode> candidates = bodies.stream().map(body -> runtime.node(body.node()))
                .flatMap(Optional::stream).toList();
        if (candidates.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException(
                    "The network of the client has none of the target nodes of the transaction"));
        }
        return runtime.execute(this, candidates, (node, timeout) -> {
            final TransactionResponse response = runtime.call(node, type.grpcMethod(), transaction(body(node)),
                    timeout);
            return Execution.Answer.precheck(Statuses.ofCode(
                    response.getNodeTransactionPrecheckCodeValue()), false,
                    () -> new Response<>(transactionId(), type.type(), node.account()));
        });
    }

    private NodeBody body(final ConsensusNode node) {
        return bodies.stream().filter(body -> body.node().equals(node.account())).findFirst().orElseThrow();
    }

    /// Reads a packed transaction from its byte form.
    ///
    /// @param bytes the bytes of a `TransactionList`
    /// @return the packed transaction
    /// @throws IllegalArgumentException if the bytes are no transaction list of signed transactions
    public static PackedTransaction<?, ?> fromBytes(final byte[] bytes) {
        try {
            final TransactionList list = TransactionList.parseFrom(bytes);
            if (list.getTransactionListCount() == 0) {
                throw new IllegalArgumentException("The bytes hold no transaction");
            }
            final List<NodeBody> bodies = new ArrayList<>();
            final List<NodeSignature> signatures = new ArrayList<>();
            TransactionBody first = null;
            for (final com.hederahashgraph.api.proto.java.Transaction transaction : list.getTransactionListList()) {
                final SignedTransaction signed = SignedTransaction.parseFrom(transaction.getSignedTransactionBytes());
                final TransactionBody body = TransactionBody.parseFrom(signed.getBodyBytes());
                first = first == null ? body : first;
                final AccountId node = Protobuf.fromProto(body.getNodeAccountID());
                bodies.add(new NodeBody(node, signed.getBodyBytes().toByteArray()));
                for (final SignaturePair pair : signed.getSigMap().getSigPairList()) {
                    signatures.add(new NodeSignature(node, Protobuf.publicKey(pair), Protobuf.signature(pair)));
                }
            }
            final TransactionId id = Protobuf.fromProto(first.getTransactionID());
            final TransactionKind<?, ?> kind = TransactionKinds.of(first);
            return kind == null ? unknown(id, signatures, bodies) : restore(id, signatures, bodies, kind);
        } catch (final InvalidProtocolBufferException invalid) {
            throw new IllegalArgumentException("The bytes are no serialized transaction", invalid);
        }
    }

    private static <R extends Receipt, T extends Transaction<R, T>> PackedTransaction<R, T> restore(
            final TransactionId id, final List<NodeSignature> signatures, final List<NodeBody> bodies,
            final TransactionKind<R, T> kind) {
        return new DefaultPackedTransaction<>(id, signatures, bodies, kind);
    }

    /// A transaction of a type that is not implemented: it can be signed and serialized, not submitted. There are no
    /// type arguments to give it, hence the raw type.
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static PackedTransaction<?, ?> unknown(final TransactionId id, final List<NodeSignature> signatures,
                                                   final List<NodeBody> bodies) {
        return new DefaultPackedTransaction(id, signatures, bodies, null);
    }

    private com.hederahashgraph.api.proto.java.Transaction transaction(final NodeBody body) {
        final SignatureMap.Builder signatures = SignatureMap.newBuilder();
        nodeSignatures().stream().filter(signature -> signature.node().equals(body.node())).forEach(signature ->
                signatures.addSigPair(Protobuf.toSignaturePair(signature.publicKey(), signature.signature())));
        final SignedTransaction signed = SignedTransaction.newBuilder().setBodyBytes(ByteString.copyFrom(body.bytes()))
                .setSigMap(signatures).build();
        return com.hederahashgraph.api.proto.java.Transaction.newBuilder()
                .setSignedTransactionBytes(signed.toByteString()).build();
    }

    private static boolean hasSigned(final List<NodeSignature> signatures, final AccountId node,
                                     final PublicKey publicKey) {
        return signatures.stream().anyMatch(s -> s.node().equals(node) && s.publicKey().equals(publicKey));
    }
}
