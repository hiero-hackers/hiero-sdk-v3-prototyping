package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseType;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptQuery;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptResponse;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.NetworkSetting;

/// The network side of a `HieroClient`: the consensus nodes, a gRPC channel per node, submission and receipt
/// polling.
///
/// Two process-wide registries bridge gaps of the specs (see the `## Questions & Comments` of `client.md` and
/// `transactions.md`): `HieroClient` is a record without its consensus nodes, so the runtime of a client is looked up
/// by the client; `Response` holds only a `TransactionId`, so the runtime that can query its receipt is looked up by
/// that id. A workaround, not a design.
public final class ClientRuntime {

    private static final Duration RECEIPT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RECEIPT_POLL_INTERVAL = Duration.ofMillis(500);
    private static final String CRYPTO_SERVICE = "proto.CryptoService";

    private static final Map<HieroClient<?>, ClientRuntime> BY_CLIENT = new ConcurrentHashMap<>();
    private static final Map<TransactionId, Pending> BY_TRANSACTION = new ConcurrentHashMap<>();

    private final List<ConsensusNode> nodes;
    private final Map<ConsensusNode, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private ClientRuntime(final NetworkSetting networkSetting) {
        this.nodes = networkSetting.getConsensusNodes().stream()
                .sorted(Comparator.comparing(node -> node.account().toString())).toList();
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("The network setting names no consensus node");
        }
    }

    /// Creates and registers the runtime of a client.
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

    /// The node a transaction of the operator is sent to.
    ///
    /// @return the node
    public ConsensusNode selectNode() {
        return nodes.getFirst();
    }

    /// The node of an account, if the network has one.
    ///
    /// @param account the account of the node
    /// @return the node
    public Optional<ConsensusNode> node(final AccountId account) {
        return nodes.stream().filter(node -> node.account().equals(account)).findFirst();
    }

    /// Submits a transaction to a node; fails if the node does not accept it (precheck).
    ///
    /// @param node        the node
    /// @param kind        the transaction type
    /// @param transaction the signed transaction for that node
    /// @param id          the id of the transaction
    /// @return completes when the node accepted the transaction
    public CompletableFuture<Void> submit(final ConsensusNode node, final TransactionKind<?, ?> kind,
                                          final Transaction transaction, final TransactionId id) {
        BY_TRANSACTION.put(id, new Pending(this, node, kind));
        return Grpc.call(channel(node), Grpc.unary(kind.service(), kind.method(), Transaction.getDefaultInstance(),
                TransactionResponse.getDefaultInstance()), transaction).thenAccept(response -> {
            final HapiTransactionStatus precheck = HapiTransactionStatus.ofCode(
                    response.getNodeTransactionPrecheckCodeValue());
            if (precheck != HapiTransactionStatus.OK) {
                BY_TRANSACTION.remove(id);
                throw new IllegalStateException("The node rejected the transaction: " + precheck);
            }
        });
    }

    /// Makes a transaction that was submitted elsewhere queryable through this runtime.
    ///
    /// @param id   the id of the transaction
    /// @param kind the transaction type
    public void track(final TransactionId id, final TransactionKind<?, ?> kind) {
        BY_TRANSACTION.putIfAbsent(id, new Pending(this, selectNode(), kind));
    }

    /// Polls the receipt of a submitted transaction until it is final.
    ///
    /// @param id the id of the transaction
    /// @return the receipt
    public static CompletableFuture<Receipt> queryReceipt(final TransactionId id) {
        final Pending pending = BY_TRANSACTION.get(id);
        if (pending == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("The transaction " + id
                    + " was not submitted through a client of this SDK"));
        }
        return CompletableFuture.supplyAsync(() -> pending.runtime().awaitReceipt(id, pending),
                pending.runtime().executor);
    }

    private Receipt awaitReceipt(final TransactionId id, final Pending pending) {
        final Query query = Query.newBuilder().setTransactionGetReceipt(TransactionGetReceiptQuery.newBuilder()
                .setHeader(QueryHeader.newBuilder().setResponseType(ResponseType.ANSWER_ONLY))
                .setTransactionID(Protobuf.toProto(id))).build();
        final long deadline = System.nanoTime() + RECEIPT_TIMEOUT.toNanos();
        HapiTransactionStatus last = HapiTransactionStatus.UNKNOWN;
        while (System.nanoTime() < deadline) {
            final Response response = Grpc.call(channel(pending.node()), Grpc.unary(CRYPTO_SERVICE,
                    "getTransactionReceipts", Query.getDefaultInstance(), Response.getDefaultInstance()), query).join();
            final TransactionGetReceiptResponse answer = response.getTransactionGetReceipt();
            final HapiTransactionStatus precheck = HapiTransactionStatus.ofCode(
                    answer.getHeader().getNodeTransactionPrecheckCodeValue());
            if (precheck == HapiTransactionStatus.OK) {
                last = HapiTransactionStatus.ofCode(answer.getReceipt().getStatusValue());
                if (!isPending(last)) {
                    return pending.kind().receipts().create(id, last,
                            Protobuf.currentRate(answer.getReceipt().getExchangeRate()),
                            Protobuf.nextRate(answer.getReceipt().getExchangeRate()), answer.getReceipt());
                }
            } else if (!isPending(precheck)) {
                throw new IllegalStateException("The receipt query was rejected: " + precheck);
            } else {
                last = precheck;
            }
            sleep();
        }
        throw new IllegalStateException("No receipt for " + id + " within " + RECEIPT_TIMEOUT.toSeconds()
                + " s, last status " + last);
    }

    private ManagedChannel channel(final ConsensusNode node) {
        return channels.computeIfAbsent(node, target -> ManagedChannelBuilder
                .forAddress(target.ip().toString(), target.port()).usePlaintext().executor(executor).build());
    }

    private static boolean isPending(final HapiTransactionStatus status) {
        return status == HapiTransactionStatus.UNKNOWN || status == HapiTransactionStatus.RECEIPT_NOT_FOUND
                || status == HapiTransactionStatus.BUSY || status == HapiTransactionStatus.PLATFORM_NOT_ACTIVE;
    }

    private static void sleep() {
        try {
            Thread.sleep(RECEIPT_POLL_INTERVAL);
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a receipt", interrupted);
        }
    }

    private record Pending(ClientRuntime runtime, ConsensusNode node, TransactionKind<?, ?> kind) {
    }
}
