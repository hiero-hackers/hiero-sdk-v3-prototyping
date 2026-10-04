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

    private final Map<String, Handler> handlers;
    private final Map<String, RuntimeSession> sessions = new ConcurrentHashMap<>();
    private @Nullable HttpServer server;

    /**
     * Creates a server.
     *
     * @param handlers the methods
     */
    JsonRpcServer(final Map<String, Handler> handlers) {
        this.handlers = Map.copyOf(handlers);
    }

    @Override
    public String handle(final String request) {
        Object id = null;
        try {
            if (!(Json.read(request) instanceof Map<?, ?> message)) {
                return error(null, RpcError.INVALID_REQUEST, "Invalid Request", Map.of());
            }
            id = message.get("id");
            final Object method = message.get("method");
            final Handler handler = method instanceof String name ? handlers.get(name) : null;
            if (handler == null) {
                return error(id, RpcError.METHOD_NOT_FOUND, "Method not found",
                        Map.of("method", String.valueOf(method)));
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
            return Json.write(response);
        } catch (final Exception e) {
            Throwable cause = e;
            while (cause instanceof CompletionException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof RpcError error) {
                return error(id, error.code(), error.getMessage(), error.data());
            }
            return error(id, RpcError.INTERNAL_ERROR, "Internal error", Map.of("message",
                    cause.getClass().getSimpleName() + ": " + cause.getMessage()));
        }
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
        return http.getAddress().getPort();
    }

    @Override
    public void stop() {
        final HttpServer http = server;
        if (http != null) {
            http.stop(0);
        }
    }

    private void exchange(final HttpExchange exchange) throws IOException {
        final String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        final byte[] response = handle(request).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(response);
        }
    }
}
