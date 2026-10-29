package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.AccountAmount;
import com.hederahashgraph.api.proto.java.CryptoTransferTransactionBody;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseType;
import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignedTransaction;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransferList;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.client.Submittable;
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Sends queries to a consensus node: free queries as they are, paid queries with a payment transaction from the
/// operator to the node that answers. The query types delegate to it.
public final class Querying {

    /// The fee the payment transaction of a query may cost, in tinybar (1 hbar).
    private static final long PAYMENT_FEE = 100_000_000L;

    private static final Duration PAYMENT_VALID_DURATION = Duration.ofSeconds(120);

    private Querying() {
    }

    /// Sends a free query.
    ///
    /// @param request the request whose execution settings apply
    /// @param query   the query
    /// @param kind    the query type
    /// @param client  the client
    /// @param <Q> the query type
    /// @param <T> the type of the value
    /// @return the answer
    public static <Q, T> CompletionStage<QueryResponse<T>> free(final Submittable<?> request, final Q query,
                                                               final QueryKind<Q, T> kind,
                                                               final HieroClient<?> client) {
        final ClientRuntime runtime = ClientRuntime.of(client);
        final QueryHeader header = QueryHeader.newBuilder().setResponseType(ResponseType.ANSWER_ONLY).build();
        return runtime.execute(request, runtime.nodes(), (node, timeout) -> {
            final Response response = runtime.call(node, kind.grpcMethod(), kind.request().apply(query, header),
                    timeout);
            return Execution.Answer.precheck(precheck(kind, response), kind.mayBePending(),
                    () -> new QueryResponse<>(kind.answer().apply(query, response), node.account()));
        });
    }

    /// Asks a node what a query costs, in a request of its own (rule 15 of "The default execution").
    ///
    /// @param request the request whose execution settings apply
    /// @param query   the query
    /// @param kind    the query type
    /// @param client  the client
    /// @param <Q> the query type
    /// @param <T> the type of the value
    /// @return the cost
    public static <Q, T> CompletionStage<NativeToken<?, ?>> cost(final Submittable<?> request, final Q query,
                                                               final QueryKind<Q, T> kind,
                                                               final HieroClient<?> client) {
        return tinybars(request, query, kind, client).thenApply(Protobuf::hbar);
    }

    /// Sends a paid query: asks for the cost, then sends the query with a payment of that cost to the node of each
    /// attempt (rules 15 and 16 of "The default execution").
    ///
    /// @param request         the request whose execution settings apply
    /// @param query           the query
    /// @param maxQueryPayment the most the query may cost, `null` for no limit
    /// @param kind            the query type
    /// @param client          the client
    /// @param <Q> the query type
    /// @param <T> the type of the value
    /// @return the answer and its cost
    public static <Q, T> CompletionStage<PaidQueryResponse<T>> paid(final Submittable<?> request, final Q query,
                                                                   final @Nullable NativeToken<?, ?> maxQueryPayment,
                                                                   final QueryKind<Q, T> kind,
                                                                   final HieroClient<?> client) {
        final ClientRuntime runtime = ClientRuntime.of(client);
        return tinybars(request, query, kind, client).thenCompose(cost -> {
            if (maxQueryPayment != null && cost > maxQueryPayment.toBaseUnits()) {
                throw new IllegalStateException("The query costs " + cost + " tinybar, more than its maxQueryPayment "
                        + maxQueryPayment);
            }
            final Payments payments = new Payments(client, cost);
            return runtime.execute(request, runtime.nodes(), (node, timeout) -> {
                final Response response = runtime.call(node, kind.grpcMethod(), kind.request().apply(query,
                        QueryHeader.newBuilder().setResponseType(ResponseType.ANSWER_ONLY)
                                .setPayment(payments.to(node.account())).build()), timeout);
                return Execution.Answer.precheck(precheck(kind, response), kind.mayBePending(),
                        () -> new PaidQueryResponse<>(kind.answer().apply(query, response), node.account(),
                                Protobuf.hbar(cost)));
            });
        });
    }

    private static <Q, T> CompletableFuture<Long> tinybars(final Submittable<?> request, final Q query,
                                                         final QueryKind<Q, T> kind, final HieroClient<?> client) {
        final ClientRuntime runtime = ClientRuntime.of(client);
        return runtime.execute(request, runtime.nodes(), (node, timeout) -> {
            final Response response = runtime.call(node, kind.grpcMethod(), kind.request().apply(query,
                    QueryHeader.newBuilder().setResponseType(ResponseType.COST_ANSWER)
                            .setPayment(payment(client, node.account(), 0)).build()), timeout);
            return Execution.Answer.precheck(precheck(kind, response), false,
                    () -> kind.header().apply(response).getCost());
        });
    }

    private static <Q, T> HapiTransactionStatus precheck(final QueryKind<Q, T> kind, final Response response) {
        return HapiTransactionStatus.ofCode(kind.header().apply(response).getNodeTransactionPrecheckCodeValue());
    }

    /// The payments of the attempts of one paid query. An attempt on the same node as the attempt before it sends
    /// the same payment again; an attempt on another node gets a new payment to that node (rule 16).
    private static final class Payments {

        private final HieroClient<?> client;
        private final long tinybars;
        private @Nullable AccountId node;
        private @Nullable Transaction payment;

        Payments(final HieroClient<?> client, final long tinybars) {
            this.client = client;
            this.tinybars = tinybars;
        }

        /// The payment of an attempt; the attempts of a request run one after the other.
        Transaction to(final AccountId target) {
            if (payment == null || !target.equals(node)) {
                node = target;
                payment = payment(client, target, tinybars);
            }
            return payment;
        }
    }

    /// The payment of a query: a transfer of the cost from the operator to the node, signed by the operator.
    private static Transaction payment(final HieroClient<?> client, final AccountId node, final long tinybars) {
        final AccountId operator = client.operatorAccount().accountId();
        final TransactionId id = Packing.transactionId(operator);
        final TransactionBody body = TransactionBody.newBuilder()
                .setTransactionID(Protobuf.toProto(id))
                .setNodeAccountID(Protobuf.toProto(node))
                .setTransactionFee(PAYMENT_FEE)
                .setTransactionValidDuration(Protobuf.toProto(PAYMENT_VALID_DURATION))
                .setCryptoTransfer(CryptoTransferTransactionBody.newBuilder().setTransfers(TransferList.newBuilder()
                        .addAccountAmounts(AccountAmount.newBuilder().setAccountID(Protobuf.toProto(operator))
                                .setAmount(-tinybars))
                        .addAccountAmounts(AccountAmount.newBuilder().setAccountID(Protobuf.toProto(node))
                                .setAmount(tinybars))))
                .build();
        final byte[] bytes = body.toByteArray();
        final NodeSignature signature = client.transactionSigner().signTransaction(bytes, node);
        final SignedTransaction signed = SignedTransaction.newBuilder().setBodyBytes(ByteString.copyFrom(bytes))
                .setSigMap(SignatureMap.newBuilder().addSigPair(Protobuf.toSignaturePair(signature.publicKey(),
                        signature.signature()))).build();
        return Transaction.newBuilder().setSignedTransactionBytes(signed.toByteString()).build();
    }
}
