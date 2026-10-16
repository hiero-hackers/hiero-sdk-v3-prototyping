package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.TransactionBody;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.Authority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.transactions.NodeBody;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.NativeToken;

/// The methods every transaction type shares (`pack`, the `sign` tiers, the batch forms), implemented once for a
/// [TransactionKind]. The transaction classes delegate to it.
///
/// @param <R> the receipt type
/// @param <T> the transaction type
public final class Packing<R extends Receipt, T extends Transaction<R, T>> {

    /// HAPI rejects a body without a fee; the default when the transaction sets none.
    private static final Hbar DEFAULT_MAX_TRANSACTION_FEE = new Hbar(2, HbarUnit.HBAR);

    /// HAPI rejects a body without a valid duration; the default when the transaction sets none.
    private static final Duration DEFAULT_VALID_DURATION = Duration.ofSeconds(120);

    /// The node of the single body of an inner batch transaction.
    private static final AccountId NO_NODE = new AccountId(0L, 0L, "", 0L, null, null);

    private final TransactionKind<R, T> kind;

    /// Creates the shared methods of a transaction type.
    ///
    /// @param kind the transaction type
    public Packing(final TransactionKind<R, T> kind) {
        this.kind = kind;
    }

    /// `Transaction.pack(payer, nodes)`.
    ///
    /// @param self  the transaction
    /// @param payer the payer
    /// @param nodes the target nodes
    /// @return the packed transaction without signatures
    public PackedTransaction<R, T> pack(final T self, final Account payer, final List<AccountId> nodes) {
        return pack(self, payer.accountId(), nodes);
    }

    /// `Transaction.signWithOperator(client)`.
    ///
    /// @param self   the transaction
    /// @param client the client
    /// @return the transaction packed for the node of the client and signed by its operator
    public PackedTransaction<R, T> signWithOperator(final T self, final HieroClient<?> client) {
        return sign(self, client.operatorAccount().accountId(), client.transactionSigner(),
                List.of(ClientRuntime.of(client).selectNode().account()));
    }

    /// `Transaction.sign(payer, nodes)`.
    ///
    /// @param self  the transaction
    /// @param payer the payer, who signs
    /// @param nodes the target nodes
    /// @return the signed packed transaction
    public PackedTransaction<R, T> sign(final T self, final Account payer, final List<AccountId> nodes) {
        return pack(self, payer, nodes).sign(payer);
    }

    /// `Transaction.sign(payerId, signer, nodes)`.
    ///
    /// @param self    the transaction
    /// @param payerId the payer
    /// @param signer  the signer
    /// @param nodes   the target nodes
    /// @return the signed packed transaction
    public PackedTransaction<R, T> sign(final T self, final AccountId payerId, final TransactionSigner signer,
                                        final List<AccountId> nodes) {
        return pack(self, payerId, nodes).sign(signer);
    }

    /// `Transaction.signWithOperatorAndSubmit(client)`.
    ///
    /// @param self   the transaction
    /// @param client the client
    /// @return the response
    public CompletionStage<Response<R>> signWithOperatorAndSubmit(final T self, final HieroClient<?> client) {
        return signWithOperator(self, client).submit(client);
    }

    /// `Transaction.packForBatch(payer, batchKey)`.
    ///
    /// @param self     the transaction
    /// @param payer    the payer
    /// @param batchKey the key that must sign the batch
    /// @return the packed inner transaction without signatures
    public PackedTransaction<R, T> packForBatch(final T self, final Account payer, final Authority batchKey) {
        return packForBatch(self, payer.accountId(), batchKey);
    }

    /// `Transaction.signForBatchWithOperator(client, batchKey)`.
    ///
    /// @param self     the transaction
    /// @param client   the client
    /// @param batchKey the key that must sign the batch
    /// @return the signed packed inner transaction
    public PackedTransaction<R, T> signForBatchWithOperator(final T self, final HieroClient<?> client,
                                                           final Authority batchKey) {
        return signForBatch(self, client.operatorAccount().accountId(), client.transactionSigner(), batchKey);
    }

    /// `Transaction.signForBatch(payer, batchKey)`.
    ///
    /// @param self     the transaction
    /// @param payer    the payer, who signs
    /// @param batchKey the key that must sign the batch
    /// @return the signed packed inner transaction
    public PackedTransaction<R, T> signForBatch(final T self, final Account payer, final Authority batchKey) {
        return packForBatch(self, payer, batchKey).sign(payer);
    }

    /// `Transaction.signForBatch(payerId, signer, batchKey)`.
    ///
    /// @param self     the transaction
    /// @param payerId  the payer
    /// @param signer   the signer
    /// @param batchKey the key that must sign the batch
    /// @return the signed packed inner transaction
    public PackedTransaction<R, T> signForBatch(final T self, final AccountId payerId, final TransactionSigner signer,
                                                final Authority batchKey) {
        return packForBatch(self, payerId, batchKey).sign(signer);
    }

    private PackedTransaction<R, T> pack(final T self, final AccountId payer, final List<AccountId> nodes) {
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("A transaction needs at least one target node");
        }
        if (new HashSet<>(nodes).size() != nodes.size()) {
            throw new IllegalArgumentException("The target nodes contain a node twice: " + nodes);
        }
        final TransactionId id = transactionId(payer);
        final List<NodeBody> bodies = nodes.stream()
                .map(node -> new NodeBody(node, body(self, id, node).build().toByteArray())).toList();
        return new DefaultPackedTransaction<>(id, List.of(), bodies, kind);
    }

    private PackedTransaction<R, T> packForBatch(final T self, final AccountId payer, final Authority batchKey) {
        final TransactionId id = transactionId(payer);
        final byte[] body = body(self, id, NO_NODE).setBatchKey(Protobuf.toProto(batchKey)).build().toByteArray();
        return new DefaultPackedTransaction<>(id, List.of(), List.of(new NodeBody(NO_NODE, body)), kind);
    }

    /// `TransactionId.generateTransactionId` takes an `Address`, so only a payer with an entity number can pay (see
    /// the `## Questions & Comments` of `ledger.md`).
    private static TransactionId transactionId(final AccountId payer) {
        final Long num = payer.num();
        if (num == null) {
            throw new IllegalArgumentException("The payer " + payer + " has no entity number");
        }
        return TransactionId.generateTransactionId(new Address(payer.shard(), payer.realm(), payer.checksum(), num));
    }

    private TransactionBody.Builder body(final T self, final TransactionId id, final AccountId node) {
        final NativeToken<?, ?> fee = self.maxTransactionFee();
        final Duration validDuration = self.validDuration();
        final TransactionBody.Builder body = TransactionBody.newBuilder()
                .setTransactionID(Protobuf.toProto(id))
                .setNodeAccountID(Protobuf.toProto(node))
                .setTransactionFee((fee == null ? DEFAULT_MAX_TRANSACTION_FEE : fee).toBaseUnits())
                .setTransactionValidDuration(Protobuf.toProto(validDuration == null ? DEFAULT_VALID_DURATION
                        : validDuration));
        final String memo = self.memo();
        if (memo != null) {
            body.setMemo(memo);
        }
        kind.body().accept(self, body);
        return body;
    }
}
