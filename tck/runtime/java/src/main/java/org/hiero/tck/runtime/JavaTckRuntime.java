package org.hiero.tck.runtime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import org.hiero.consensusnode.client.NodeSignature;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.queries.Query;
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.keys.PrivateKey;
import org.hiero.tck.contract.Converters;
import org.hiero.tck.contract.Handler;
import org.hiero.tck.contract.Session;
import org.hiero.tck.contract.Source;
import org.hiero.tck.contract.TckRuntime;
import org.hiero.tck.contract.TckServer;
import org.jspecify.annotations.Nullable;

/**
 * The Java runtime of the TCK server: access to the JSON parameters, the execution flow (a transaction is signed by
 * the operator and the additional signers, sent and answered with its receipt; a query is sent and answered with the
 * value of its response) and the JSON-RPC server with the runtime methods {@code setup}, {@code reset} and
 * {@code generateKey}. Found by the generated server with {@link java.util.ServiceLoader}.
 */
public final class JavaTckRuntime implements TckRuntime {

    /**
     * The HAPI code of {@code SUCCESS}. {@code BasicTransactionStatus} has no such constant yet (a gap of the specs),
     * so the code is used.
     */
    private static final int SUCCESS = 22;

    private final JavaConverters converters = new JavaConverters();

    /** Creates the runtime (used by the service loader). */
    public JavaTckRuntime() {
    }

    @Override
    public Converters converters() {
        return converters;
    }

    // --- JSON parameters -----------------------------------------------------------------------------

    @Override
    public <T> @Nullable T value(final Map<String, Object> json,
                                  final List<? extends Source<? extends T>> sources) {
        for (final Source<? extends T> source : sources) {
            final Object value = get(json, source.path());
            if (value != null) {
                return source.converter().apply(value);
            }
        }
        return null;
    }

    @Override
    public <T> T required(final @Nullable T value, final String parameters) {
        if (value == null) {
            throw RpcError.internal("the API requires a value for " + parameters);
        }
        return value;
    }

    @Override
    public <T> T or(final @Nullable T value, final T defaultValue) {
        return value == null ? defaultValue : value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable Map<String, Object> object(final Map<String, Object> json, final String path) {
        return get(json, path) instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> objects(final Map<String, Object> json, final String path) {
        return get(json, path) instanceof List<?> list ? list.stream().filter(Map.class::isInstance)
                .map(e -> (Map<String, Object>) e).toList() : List.of();
    }

    @Override
    public void unsupported(final Map<String, Object> json, final String name, final String reason) {
        if (json.get(name) != null) {
            throw RpcError.gap(name + ": " + reason);
        }
    }

    private static @Nullable Object get(final Map<String, Object> json, final String path) {
        Object current = json;
        for (final String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(segment);
        }
        return current;
    }

    // --- execution -----------------------------------------------------------------------------------

    @Override
    public <R extends Receipt> R transaction(final Session session, final Transaction<R, ?> transaction,
                                             final @Nullable Map<String, Object> common) {
        final RuntimeSession runtime = RuntimeSession.of(session);
        PackedTransaction<R, ?> packed = transaction.signWithOperator(runtime.client());
        if (common != null && common.get("signers") instanceof List<?> signers) {
            for (final Object signer : signers) {
                packed = packed.sign(signer(converters.privateKey(signer)));
            }
        }
        final Response<R> response = join(packed.submit(runtime.client()));
        final R receipt = join(response.queryReceipt());
        if (receipt.status().code() != SUCCESS) {
            throw new RpcError(RpcError.HIERO_ERROR, "Hiero error",
                    Map.of("status", converters.statusJson(receipt.status())));
        }
        return receipt;
    }

    /** A signer for a bare private key: signs the body of every node with the key. */
    private static TransactionSigner signer(final PrivateKey key) {
        return (bytes, node) -> new NodeSignature(node, key.createPublicKey(), key.sign(bytes));
    }

    @Override
    public <T, Q extends QueryResponse<T>> T query(final Session session, final Query<T, Q> query) {
        return join(query.submit(RuntimeSession.of(session).client())).value();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void put(final Map<String, Object> result, final String path, final @Nullable T value,
                        final Function<? super T, Object> converter) {
        if (value == null) {
            return;
        }
        final String[] segments = path.split("\\.");
        Map<String, Object> target = result;
        for (int i = 0; i < segments.length - 1; i++) {
            target = (Map<String, Object>) target.computeIfAbsent(segments[i], k -> new LinkedHashMap<>());
        }
        target.put(segments[segments.length - 1], converter.apply(value));
    }

    private static <T> T join(final CompletionStage<T> stage) {
        return stage.toCompletableFuture().join();
    }

    // --- server --------------------------------------------------------------------------------------

    @Override
    public TckServer server(final Map<String, Handler> methods) {
        final Map<String, Handler> all = new LinkedHashMap<>(methods);
        final Utilities utilities = new Utilities(this, converters);
        all.put("setup", utilities::setup);
        all.put("reset", utilities::reset);
        all.put("generateKey", utilities::generateKey);
        return new JsonRpcServer(all);
    }
}
