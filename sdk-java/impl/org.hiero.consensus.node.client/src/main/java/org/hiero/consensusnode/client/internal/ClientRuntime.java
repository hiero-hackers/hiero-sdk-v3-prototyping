package org.hiero.consensusnode.client.internal;

import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseType;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptQuery;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.hiero.consensusnode.client.AttemptOutcome;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.Submittable;
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.NetworkSetting;
import org.jspecify.annotations.Nullable;

/// The network side of a `HieroClient`: the consensus nodes, a gRPC channel per node, and the execution of requests
/// in attempts.
///
/// A process-wide registry bridges a gap of the specs (see the `## Questions & Comments` of `client.md`):
/// `HieroClient` is a record without its consensus nodes, so the runtime of a client is looked up by the client. A
/// workaround, not a design.
public final class ClientRuntime {

    private static final MethodDescriptor<Query, Response> RECEIPT_METHOD = Grpc.unary("proto.CryptoService",
            "getTransactionReceipts", Query.getDefaultInstance(), Response.getDefaultInstance());

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

    /// All consensus nodes of the network: the candidates of a request that is not bound to nodes (rule 3 of the
    /// section "The default execution" of `client.md`).
    ///
    /// @return the nodes
    public List<ConsensusNode> nodes() {
        return nodes;
    }

    /// Executes a request in attempts on the executor of the client, as the execution settings of the client and
    /// the request define.
    ///
    /// @param request    the request
    /// @param candidates the nodes its attempts may go to
    /// @param send       sends one attempt
    /// @param <T> the type of the value
    /// @return the value of the successful attempt
    public <T> CompletableFuture<T> execute(final Submittable<?> request, final List<ConsensusNode> candidates,
                                            final Execution.Send<T> send) {
        return CompletableFuture.supplyAsync(() -> Execution.run(client.executionSettings(), request, candidates,
                send), executor);
    }

    /// Calls a unary method of a node and waits for the answer.
    ///
    /// @param node    the node
    /// @param method  the method
    /// @param request the request message
    /// @param timeout the time the call may take
    /// @param <RequestT>  the request type
    /// @param <ResponseT> the response type
    /// @return the answer
    /// @throws TransportException if no answer arrived
    <RequestT, ResponseT> ResponseT call(final ConsensusNode node, final MethodDescriptor<RequestT, ResponseT> method,
                                         final RequestT request, final Duration timeout) {
        try {
            return Grpc.call(channel(node), method, request, timeout).join();
        } catch (final CompletionException failed) {
            if (failed.getCause() instanceof TransportException transport) {
                throw transport;
            }
            throw failed;
        }
    }

    /// Reads the receipt of a transaction once it is final (`TransactionReceiptQuery`). An attempt is pending while
    /// the node answers `UNKNOWN` or `RECEIPT_NOT_FOUND` and while the receipt's status is `UNKNOWN` or `OK`.
    ///
    /// @param request           the request the receipt is read for
    /// @param node              the account of the node to ask; `null` for any node of the client
    /// @param id                the id of the transaction
    /// @param includeDuplicates whether to ask for the receipts of duplicates
    /// @param includeChildren   whether to ask for the receipts of children
    /// @return the answer of the node and the node that gave it
    public CompletableFuture<QueryResponse<TransactionGetReceiptResponse>> queryReceipt(final Submittable<?> request,
            final @Nullable AccountId node, final TransactionId id, final boolean includeDuplicates,
            final boolean includeChildren) {
        final List<ConsensusNode> candidates = node == null ? nodes : node(node).map(List::of).orElse(List.of());
        if (candidates.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(
                    "The network of the client has no consensus node " + node));
        }
        final Query query = Query.newBuilder().setTransactionGetReceipt(TransactionGetReceiptQuery.newBuilder()
                .setHeader(QueryHeader.newBuilder().setResponseType(ResponseType.ANSWER_ONLY))
                .setTransactionID(Protobuf.toProto(id)).setIncludeDuplicates(includeDuplicates)
                .setIncludeChildReceipts(includeChildren)).build();
        return execute(request, candidates, (target, timeout) -> {
            final TransactionGetReceiptResponse answer = call(target, RECEIPT_METHOD, query, timeout)
                    .getTransactionGetReceipt();
            final HapiTransactionStatus precheck = HapiTransactionStatus.ofCode(
                    answer.getHeader().getNodeTransactionPrecheckCodeValue());
            if (precheck != HapiTransactionStatus.OK) {
                return Execution.Answer.precheck(precheck, true, () -> null);
            }
            final HapiTransactionStatus status = HapiTransactionStatus.ofCode(answer.getReceipt().getStatusValue());
            final AttemptOutcome outcome = Execution.receiptOutcome(status);
            return new Execution.Answer<>(status, outcome, outcome == AttemptOutcome.SUCCESS
                    ? new QueryResponse<>(answer, target.account()) : null);
        });
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

    /// Waits.
    ///
    /// @param duration how long
    static void sleep(final Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the next attempt", interrupted);
        }
    }
}
