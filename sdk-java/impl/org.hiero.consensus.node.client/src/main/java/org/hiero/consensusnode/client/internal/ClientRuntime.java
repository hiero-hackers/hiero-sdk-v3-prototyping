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
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.NetworkSetting;
import org.jspecify.annotations.Nullable;

/// The network side of a `HieroClient`: the consensus nodes, a gRPC channel per node, submission and receipt
/// polling.
///
/// A process-wide registry bridges a gap of the specs (see the `## Questions & Comments` of `client.md`):
/// `HieroClient` is a record without its consensus nodes, so the runtime of a client is looked up by the client. A
/// workaround, not a design.
public final class ClientRuntime {

    private static final Duration RECEIPT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RECEIPT_POLL_INTERVAL = Duration.ofMillis(500);
    private static final String CRYPTO_SERVICE = "proto.CryptoService";

    private static final Map<HieroClient<?>, ClientRuntime> BY_CLIENT = new ConcurrentHashMap<>();

    private final HieroClient<?> client;
    private final List<ConsensusNode> nodes;
    private final Map<ConsensusNode, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private ClientRuntime(final HieroClient<?> client, final NetworkSetting networkSetting) {
        this.client = client;
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
        BY_CLIENT.put(client, new ClientRuntime(client, networkSetting));
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
        return Grpc.call(channel(node), kind.grpcMethod(), transaction).thenAccept(response -> {
            final HapiTransactionStatus precheck = HapiTransactionStatus.ofCode(
                    response.getNodeTransactionPrecheckCodeValue());
            if (precheck != HapiTransactionStatus.OK) {
                throw new PrecheckException(precheck);
            }
        });
    }

    /// Polls the receipt of a transaction until it is final (`TransactionReceiptQuery`). Waits while the node
    /// answers `BUSY`, `UNKNOWN`, `RECEIPT_NOT_FOUND` or `PLATFORM_NOT_ACTIVE`, and while the receipt's status is
    /// `UNKNOWN` or `OK`.
    ///
    /// @param node              the account of the node to ask; `null` for any node of the client
    /// @param id                the id of the transaction
    /// @param includeDuplicates whether to ask for the receipts of duplicates
    /// @param includeChildren   whether to ask for the receipts of children
    /// @return the answer of the node and the node that gave it
    public CompletableFuture<QueryResponse<TransactionGetReceiptResponse>> queryReceipt(final @Nullable AccountId node,
            final TransactionId id, final boolean includeDuplicates, final boolean includeChildren) {
        return CompletableFuture.supplyAsync(() -> {
            final ConsensusNode target = node == null ? selectNode() : node(node).orElseThrow(
                    () -> new IllegalArgumentException("The network of the client has no consensus node " + node));
            return new QueryResponse<>(awaitReceipt(target, id, includeDuplicates, includeChildren),
                    target.account());
        }, executor);
    }

    private TransactionGetReceiptResponse awaitReceipt(final ConsensusNode node, final TransactionId id,
                                                       final boolean includeDuplicates,
                                                       final boolean includeChildren) {
        final Query query = Query.newBuilder().setTransactionGetReceipt(TransactionGetReceiptQuery.newBuilder()
                .setHeader(QueryHeader.newBuilder().setResponseType(ResponseType.ANSWER_ONLY))
                .setTransactionID(Protobuf.toProto(id)).setIncludeDuplicates(includeDuplicates)
                .setIncludeChildReceipts(includeChildren)).build();
        final long deadline = System.nanoTime() + RECEIPT_TIMEOUT.toNanos();
        HapiTransactionStatus last = HapiTransactionStatus.UNKNOWN;
        while (System.nanoTime() < deadline) {
            final Response response = Grpc.call(channel(node), Grpc.unary(CRYPTO_SERVICE,
                    "getTransactionReceipts", Query.getDefaultInstance(), Response.getDefaultInstance()), query).join();
            final TransactionGetReceiptResponse answer = response.getTransactionGetReceipt();
            final HapiTransactionStatus precheck = HapiTransactionStatus.ofCode(
                    answer.getHeader().getNodeTransactionPrecheckCodeValue());
            if (precheck == HapiTransactionStatus.OK) {
                last = HapiTransactionStatus.ofCode(answer.getReceipt().getStatusValue());
                if (last != HapiTransactionStatus.UNKNOWN && last != HapiTransactionStatus.OK) {
                    return answer;
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

    /// The executor of the blocking work of this runtime (virtual threads).
    ///
    /// @return the executor
    ExecutorService executor() {
        return executor;
    }

    /// The channel to a node.
    ///
    /// @param node the node
    /// @return the channel, created on first use
    ManagedChannel channel(final ConsensusNode node) {
        return channels.computeIfAbsent(node, target -> ManagedChannelBuilder
                .forAddress(target.ip().toString(), target.port()).usePlaintext().executor(executor).build());
    }

    private static boolean isPending(final HapiTransactionStatus status) {
        return status == HapiTransactionStatus.UNKNOWN || status == HapiTransactionStatus.RECEIPT_NOT_FOUND
                || status == HapiTransactionStatus.BUSY || status == HapiTransactionStatus.PLATFORM_NOT_ACTIVE;
    }

    private static void sleep() {
        sleep(RECEIPT_POLL_INTERVAL);
    }

    /// Waits.
    ///
    /// @param duration how long
    static void sleep(final Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a receipt", interrupted);
        }
    }
}
