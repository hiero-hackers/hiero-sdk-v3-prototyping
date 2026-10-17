package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.AccountAmount;
import com.hederahashgraph.api.proto.java.CryptoTransferTransactionBody;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseHeader;
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
import org.hiero.consensusnode.queries.PaidQueryResponse;
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// Sends queries to a consensus node: free queries as they are, paid queries with a payment transaction from the
/// operator to the node that answers. The query types delegate to it.
public final class Querying {

    /// The fee the payment transaction of a query may cost, in tinybar (1 hbar).
    private static final long PAYMENT_FEE = 100_000_000L;

    private static final Duration PAYMENT_VALID_DURATION = Duration.ofSeconds(120);

    private static final int MAX_ATTEMPTS = 5;

    private static final Duration BUSY_BACKOFF = Duration.ofMillis(500);

    private Querying() {
    }

    /// Sends a free query.
    ///
    /// @param query  the query
    /// @param kind   the query type
    /// @param client the client
    /// @param <Q> the query type
    /// @param <T> the type of the value
    /// @return the answer
    public static <Q, T> CompletionStage<QueryResponse<T>> free(final Q query, final QueryKind<Q, T> kind,
                                                               final HieroClient<?> client) {
        final ClientRuntime runtime = ClientRuntime.of(client);
        return CompletableFuture.supplyAsync(() -> {
            final ConsensusNode node = runtime.selectNode();
            final Response response = call(runtime, node, kind, query, QueryHeader.newBuilder()
                    .setResponseType(ResponseType.ANSWER_ONLY).build());
            return new QueryResponse<>(kind.answer().apply(query, response), node.account());
        }, runtime.executor());
    }

    /// Asks a node what a query costs.
    ///
    /// @param query  the query
    /// @param kind   the query type
    /// @param client the client
    /// @param <Q> the query type
    /// @param <T> the type of the value
    /// @return the cost
    public static <Q, T> CompletionStage<NativeToken<?, ?>> cost(final Q query, final QueryKind<Q, T> kind,
                                                               final HieroClient<?> client) {
        final ClientRuntime runtime = ClientRuntime.of(client);
        return CompletableFuture.supplyAsync(() -> Protobuf.hbar(cost(runtime, runtime.selectNode(), query, kind,
                client)), runtime.executor());
    }

    /// Sends a paid query: asks for the cost, then sends the query with a payment of that cost.
    ///
    /// @param query           the query
    /// @param maxQueryPayment the most the query may cost, `null` for no limit
    /// @param kind            the query type
    /// @param client          the client
    /// @param <Q> the query type
    /// @param <T> the type of the value
    /// @return the answer and its cost
    public static <Q, T> CompletionStage<PaidQueryResponse<T>> paid(final Q query,
                                                                   final @Nullable NativeToken<?, ?> maxQueryPayment,
                                                                   final QueryKind<Q, T> kind,
                                                                   final HieroClient<?> client) {
        final ClientRuntime runtime = ClientRuntime.of(client);
        return CompletableFuture.supplyAsync(() -> {
            final ConsensusNode node = runtime.selectNode();
            final long cost = cost(runtime, node, query, kind, client);
            if (maxQueryPayment != null && cost > maxQueryPayment.toBaseUnits()) {
                throw new IllegalStateException("The query costs " + cost + " tinybar, more than its maxQueryPayment "
                        + maxQueryPayment);
            }
            final Response response = call(runtime, node, kind, query, QueryHeader.newBuilder()
                    .setResponseType(ResponseType.ANSWER_ONLY).setPayment(payment(client, node.account(), cost))
                    .build());
            return new PaidQueryResponse<>(kind.answer().apply(query, response), node.account(),
                    Protobuf.hbar(cost));
        }, runtime.executor());
    }

    private static <Q, T> long cost(final ClientRuntime runtime, final ConsensusNode node, final Q query,
                                    final QueryKind<Q, T> kind, final HieroClient<?> client) {
        final Response response = call(runtime, node, kind, query, QueryHeader.newBuilder()
                .setResponseType(ResponseType.COST_ANSWER).setPayment(payment(client, node.account(), 0)).build());
        return kind.header().apply(response).getCost();
    }

    /// Sends the query and checks the precheck status; retries while the node is busy.
    private static <Q, T> Response call(final ClientRuntime runtime, final ConsensusNode node,
                                        final QueryKind<Q, T> kind, final Q query, final QueryHeader header) {
        for (int attempt = 1; ; attempt++) {
            final Response response = Grpc.call(runtime.channel(node), kind.grpcMethod(),
                    kind.request().apply(query, header)).join();
            final ResponseHeader answer = kind.header().apply(response);
            final HapiTransactionStatus precheck = HapiTransactionStatus.ofCode(
                    answer.getNodeTransactionPrecheckCodeValue());
            if (precheck == HapiTransactionStatus.OK) {
                return response;
            }
            if (attempt >= MAX_ATTEMPTS || (precheck != HapiTransactionStatus.BUSY
                    && precheck != HapiTransactionStatus.PLATFORM_NOT_ACTIVE)) {
                throw new PrecheckException(precheck);
            }
            ClientRuntime.sleep(BUSY_BACKOFF);
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
