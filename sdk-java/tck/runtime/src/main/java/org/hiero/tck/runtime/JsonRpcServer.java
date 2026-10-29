package org.hiero.tck.runtime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import org.hiero.tck.contract.Handler;
import org.hiero.tck.contract.TckServer;
import org.jspecify.annotations.Nullable;

/**
 * The JSON-RPC 2.0 server that the TCK test driver talks to (by default on port 8544). Every request carries the
 * {@code sessionId} of its test file, so each test file gets its own client ({@link RuntimeSession}).
 */
final class JsonRpcServer implements TckServer {

    /** The beginning of the message of an error of the SDK for a request a node rejected in precheck. */
    private static final String PRECHECK_PREFIX = "precheck failed: ";

    private final Map<String, Handler> handlers;
    private final Map<String, RuntimeSession> sessions = new ConcurrentHashMap<>();
    private final ServerLog log = new ServerLog();
    private final TestLog testLog = new TestLog();
    private @Nullable HttpServer server;

    /**
     * Creates a server.
     *
     * @param handlers the methods
     */
    JsonRpcServer(final Map<String, Handler> handlers) {
        this.handlers = Map.copyOf(handlers);
    }

    /// The response to a request, and the exception the call failed with (for the log of the test).
    private record Answer(String response, @Nullable Throwable cause) {
    }

    @Override
    public String handle(final String request) {
        return answer(request).response();
    }

    private Answer answer(final String request) {
        Object id = null;
        String name = null;
        try {
            if (!(Json.read(request) instanceof Map<?, ?> message)) {
                return new Answer(error(null, RpcError.INVALID_REQUEST, "Invalid Request", Map.of()), null);
            }
            id = message.get("id");
            final Object method = message.get("method");
            name = method instanceof String text ? text : null;
            final Handler handler = name != null ? handlers.get(name) : null;
            if (handler == null) {
                log.failed(name, RpcError.METHOD_NOT_FOUND, "no binding for this TCK method", null);
                return new Answer(error(id, RpcError.METHOD_NOT_FOUND, "Method not found",
                        Map.of("method", String.valueOf(method))), null);
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> params = message.get("params") instanceof Map<?, ?> map
                    ? (Map<String, Object>) map : Map.of();
            final String sessionId = String.valueOf(params.getOrDefault("sessionId", ""));
            final RuntimeSession session = sessions.computeIfAbsent(sessionId, RuntimeSession::new);
            final Map<String, Object> response = new LinkedHashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            response.put("result", handler.handle(params, session));
            return new Answer(Json.write(response), null);
        } catch (final Throwable e) {
            // also Error: a NoClassDefFoundError must become a JSON-RPC error, not a request that never answers
            Throwable cause = e;
            while (cause instanceof CompletionException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            // a node rejected the request before consensus: the SDK reports the status in a fixed message form,
            // as the specs have no error type with a status yet (see the Questions & Comments of transactions.md);
            // the error the request fails with when its retry policy gives up carries it as its cause
            final Throwable precheck = precheck(cause);
            if (precheck != null) {
                final String status = precheck.getMessage().substring(PRECHECK_PREFIX.length());
                log.failed(name, RpcError.HIERO_ERROR, status, cause);
                return new Answer(error(id, RpcError.HIERO_ERROR, "Hiero error", Map.of("status", status)),
                        cause);
            }
            if (cause instanceof RpcError error) {
                log.failed(name, error.code(), detail(error), error);
                return new Answer(error(id, error.code(), error.getMessage(), error.data()), error);
            }
            final String detail = cause.getClass().getSimpleName() + ": " + cause.getMessage();
            log.failed(name, RpcError.INTERNAL_ERROR, detail, cause);
            return new Answer(error(id, RpcError.INTERNAL_ERROR, "Internal error", Map.of("message", detail)),
                    cause);
        }
    }

    /// The `data.message` of an error, or its whole data object when it carries no message (a Hiero
    /// error reports `data.status` instead).
    private static String detail(final RpcError error) {
        final Object message = error.data().get("message");
        return message != null ? String.valueOf(message) : String.valueOf(error.data());
    }

    private static String error(final @Nullable Object id, final int code, final String message,
                                final Map<String, Object> data) {
        final Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("data", data);
        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", error);
        return Json.write(response);
    }

    @Override
    public int start(final int port) throws IOException {
        final HttpServer http = HttpServer.create(new InetSocketAddress(port), 0);
        http.createContext("/", this::exchange);
        http.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        http.start();
        server = http;
        // the generated TckMain never calls stop(): the server runs until it is killed
        Runtime.getRuntime().addShutdownHook(new Thread(log::summary, "tck-server-summary"));
        return http.getAddress().getPort();
    }

    @Override
    public void stop() {
        final HttpServer http = server;
        if (http != null) {
            http.stop(0);
        }
        log.summary();
    }

    private void exchange(final HttpExchange exchange) throws IOException {
        final String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        final Answer answer = answer(request);
        testLog.call(exchange.getRequestHeaders().getFirst(TestLog.TEST_ID_HEADER), request, answer.response(),
                answer.cause());
        final byte[] response = answer.response().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(response);
        }
    }

    /// The error in the chain of causes that reports a rejected precheck, if any.
    private static @Nullable Throwable precheck(final Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().startsWith(PRECHECK_PREFIX)) {
                return cause;
            }
        }
        return null;
    }
}
