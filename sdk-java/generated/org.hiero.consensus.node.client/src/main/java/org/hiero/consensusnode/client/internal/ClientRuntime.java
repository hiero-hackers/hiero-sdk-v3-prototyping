package org.hiero.consensusnode.client.internal;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.transactions.HapiTransactionStatus;
import org.hiero.consensusnode.transactions.Receipt;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseType;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptQuery;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptResponse;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.NetworkSetting;
import org.jspecify.annotations.Nullable;

/// The state a [HieroClient] needs but cannot hold: the consensus nodes of its network and the gRPC
/// channels to them.
///
/// `HieroClient` is a record of operator, network and signer, and `Network` carries no nodes — the
/// [NetworkSetting] that names them reaches `ClientFactory` and ends there. A submitted transaction has
/// the same problem: `Response` is a record of nothing but a transaction id, yet `queryReceipt()` has to
/// reach the network. Both are bridged here by a registry, keyed by the client and by the transaction id.
public final class ClientRuntime {

    /// How long `queryReceipt` waits for consensus before it gives up.
    private static final Duration RECEIPT_TIMEOUT = Duration.ofSeconds(30);

    /// How long `queryReceipt` waits between two attempts.
    private static final Duration RECEIPT_POLL_INTERVAL = Duration.ofMillis(500);

    private static final String CRYPTO_SERVICE = "proto.CryptoService";

    private static final Map<HieroClient<?>, ClientRuntime> BY_CLIENT = new ConcurrentHashMap<>();

    private static final Map<TransactionId, Pending> BY_TRANSACTION = new ConcurrentHashMap<>();

    private final NetworkSetting networkSetting;
    private final List<ConsensusNode> nodes;
    private final Map<ConsensusNode, Channel> channels = new ConcurrentHashMap<>();
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    private ClientRuntime(final NetworkSetting networkSetting) {
        this.networkSetting = Objects.requireNonNull(networkSetting, "networkSetting must not be null");
        this.nodes = networkSetting.getConsensusNodes().stream()
                .sorted(Comparator.comparing(node -> String.valueOf(node.account())))
                .toList();
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("The network setting names no consensus node");
        }
    }

    /// Registers the runtime of a client.
    ///
    /// @param client         the client
    /// @param networkSetting the network it talks to
    public static void register(final HieroClient<?> client, final NetworkSetting networkSetting) {
        BY_CLIENT.put(client, new ClientRuntime(networkSetting));
    }

    /// Returns the runtime of a client.
    ///
    /// @param client the client
    /// @return the runtime
    /// @throws IllegalStateException if the client was not created by `ClientFactory`
    public static ClientRuntime of(final HieroClient<?> client) {
        final ClientRuntime runtime = BY_CLIENT.get(client);
        if (runtime == null) {
            throw new IllegalStateException("The client was not created by ClientFactory: " + client);
        }
        return runtime;
    }

    /// Returns the node a transaction is sent to.
    ///
    /// @return the node
    public ConsensusNode selectNode() {
        return nodes.getFirst();
    }

    /// Returns the network setting of the client.
    ///
    /// @return the setting
    public NetworkSetting networkSetting() {
        return networkSetting;
    }

    private Channel channel(final ConsensusNode node) {
        return channels.computeIfAbsent(node, target -> ManagedChannelBuilder
                .forAddress(target.ip().toString(), target.port())
                .usePlaintext()
                .executor(executor)
                .build());
    }

    /// Sends a signed transaction to a node and remembers how its receipt is read.
    ///
    /// @param node        the node
    /// @param service     the qualified name of the service the transaction belongs to
    /// @param method      the name of the method that accepts it
    /// @param transaction the signed transaction
    /// @param id          the transaction id
    /// @param receipts    builds the typed receipt from the protobuf receipt
    /// @return completes once the node has accepted the transaction
    public CompletableFuture<Void> submit(final ConsensusNode node, final String service, final String method,
                                          final Transaction transaction, final TransactionId id,
                                          final ReceiptFactory receipts) {
        BY_TRANSACTION.put(id, new Pending(this, node, receipts));
        return Grpc.call(channel(node),
                        Grpc.unary(service, method,
                                Transaction::getDefaultInstance, TransactionResponse::getDefaultInstance),
                        transaction)
                .thenAccept(response -> {
                    final HapiTransactionStatus precheck =
                            Protobuf.fromProto(response.getNodeTransactionPrecheckCode());
                    if (precheck != HapiTransactionStatus.OK) {
                        BY_TRANSACTION.remove(id);
                        throw new IllegalStateException("The node rejected the transaction: " + precheck);
                    }
                });
    }

    /// Queries the receipt of a submitted transaction, waiting for consensus.
    ///
    /// @param id the transaction id
    /// @return the receipt
    /// @throws IllegalStateException if the transaction was not submitted through this process
    public static CompletableFuture<Receipt> queryReceipt(final TransactionId id) {
        final Pending pending = BY_TRANSACTION.get(id);
        if (pending == null) {
            throw new IllegalStateException("The transaction " + id + " was not submitted by this client");
        }
        return CompletableFuture.supplyAsync(() -> pending.runtime().awaitReceipt(id, pending),
                pending.runtime().executor);
    }

    private Receipt awaitReceipt(final TransactionId id, final Pending pending) {
        final Query query = Query.newBuilder()
                .setTransactionGetReceipt(TransactionGetReceiptQuery.newBuilder()
                        .setHeader(QueryHeader.newBuilder().setResponseType(ResponseType.ANSWER_ONLY))
                        .setTransactionID(Protobuf.toProto(id)))
                .build();
        final long deadline = System.nanoTime() + RECEIPT_TIMEOUT.toNanos();
        HapiTransactionStatus last = HapiTransactionStatus.UNKNOWN;
        while (System.nanoTime() < deadline) {
            final Response response = Grpc.call(channel(pending.node()),
                    Grpc.unary(CRYPTO_SERVICE, "getTransactionReceipts",
                            Query::getDefaultInstance, Response::getDefaultInstance),
                    query).join();
            final TransactionGetReceiptResponse receipt = response.getTransactionGetReceipt();
            final HapiTransactionStatus precheck =
                    Protobuf.fromProto(receipt.getHeader().getNodeTransactionPrecheckCode());
            if (precheck == HapiTransactionStatus.OK) {
                last = Protobuf.fromProto(receipt.getReceipt().getStatus());
                if (!isPending(last)) {
                    return pending.receipts().create(id, last,
                            Protobuf.currentRate(receipt.getReceipt().getExchangeRate()),
                            Protobuf.nextRate(receipt.getReceipt().getExchangeRate()),
                            receipt.getReceipt());
                }
            } else if (!isPending(precheck)) {
                throw new IllegalStateException("The receipt query was rejected: " + precheck);
            } else {
                last = precheck;
            }
            sleep();
        }
        throw new IllegalStateException(
                "No receipt for " + id + " within " + RECEIPT_TIMEOUT.toSeconds() + " s, last status " + last);
    }

    private static boolean isPending(final HapiTransactionStatus status) {
        return status == HapiTransactionStatus.UNKNOWN
                || status == HapiTransactionStatus.RECEIPT_NOT_FOUND
                || status == HapiTransactionStatus.BUSY
                || status == HapiTransactionStatus.PLATFORM_NOT_ACTIVE;
    }

    private static void sleep() {
        try {
            Thread.sleep(RECEIPT_POLL_INTERVAL);
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a receipt", interrupted);
        }
    }

    /// Builds the typed receipt of a transaction from the protobuf receipt.
    @FunctionalInterface
    public interface ReceiptFactory {

        /// Creates the receipt.
        ///
        /// @param id               the transaction id
        /// @param status           the status the network reached
        /// @param exchangeRate     the current exchange rate
        /// @param nextExchangeRate the next exchange rate
        /// @param receipt          the protobuf receipt
        /// @return the receipt
        Receipt create(TransactionId id, org.hiero.consensusnode.transactions.TransactionStatus status,
                       org.hiero.nativeToken.ExchangeRate exchangeRate,
                       org.hiero.nativeToken.ExchangeRate nextExchangeRate,
                       com.hederahashgraph.api.proto.java.TransactionReceipt receipt);
    }

    private record Pending(ClientRuntime runtime, ConsensusNode node, ReceiptFactory receipts) {
    }

    /// Forgets every registered client and transaction. Used by the TCK between test files.
    public static void reset() {
        BY_TRANSACTION.clear();
    }

    /// Returns the nodes of the network, in a stable order.
    ///
    /// @return the nodes
    public List<ConsensusNode> nodes() {
        return new ArrayList<>(nodes);
    }

    /// Returns the runtime of a client, or `null` if it has none.
    ///
    /// @param client the client
    /// @return the runtime or `null`
    public static @Nullable ClientRuntime find(final HieroClient<?> client) {
        return BY_CLIENT.get(client);
    }
}
