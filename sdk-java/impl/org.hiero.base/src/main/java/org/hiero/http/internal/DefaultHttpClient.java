package org.hiero.http.internal;

import java.net.URI;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hiero.http.Cancellation;
import org.hiero.http.CancellationRegistration;
import org.hiero.http.CancelledException;
import org.hiero.http.ClientClosedException;
import org.hiero.http.ConnectionException;
import org.hiero.http.HttpClient;
import org.hiero.http.HttpConfiguration;
import org.hiero.http.HttpRequest;
import org.hiero.http.HttpResponse;

/// The default `HttpClient` of the SDK: a wrapper of `java.net.http.HttpClient`.
///
/// Redirects are not followed (the client has no policies); a status code is never interpreted. `close()` shuts the
/// wrapped client down and waits for its in-flight exchanges; `close(closeTimeout)` aborts whatever is left after the
/// timeout and fails the aborted exchanges with `ClientClosedException`.
public final class DefaultHttpClient extends HttpClient {

    private final java.net.http.HttpClient client;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Set<CompletableFuture<HttpResponse>> inFlight = ConcurrentHashMap.newKeySet();

    /// Creates a client that applies `configuration` to every exchange.
    ///
    /// @param configuration the configuration
    /// @throws NullPointerException if `configuration` is `null`
    public DefaultHttpClient(final HttpConfiguration configuration) {
        super(configuration);
        this.client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(configuration.connectTimeout())
                .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
                .build();
    }

    @Override
    public CompletionStage<HttpResponse> execute(final HttpRequest request, final Cancellation cancellation) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(cancellation, "cancellation must not be null");
        final CompletableFuture<HttpResponse> result = new CompletableFuture<>();
        if (closed.get()) {
            result.completeExceptionally(new ClientClosedException("the HTTP client has been closed", null));
            return result;
        }
        if (cancellation.isCancelled()) {
            result.completeExceptionally(new CancelledException("the exchange was cancelled before it started", null));
            return result;
        }
        final java.net.http.HttpRequest nativeRequest;
        try {
            nativeRequest = toNativeRequest(request);
        } catch (final IllegalArgumentException e) {
            // a header the native stack does not allow a caller to set (e.g. "host"); no response can be produced
            result.completeExceptionally(new ConnectionException("the request cannot be sent: " + e.getMessage(), e));
            return result;
        }
        inFlight.add(result);
        final CompletableFuture<java.net.http.HttpResponse<byte[]>> exchange;
        try {
            exchange = client.sendAsync(nativeRequest, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
        } catch (final RuntimeException e) {
            inFlight.remove(result);
            result.completeExceptionally(closed.get()
                    ? new ClientClosedException("the HTTP client has been closed", e)
                    : new ConnectionException("the request could not be submitted", e));
            return result;
        }
        final CancellationRegistration registration = cancellation.onCancel(() -> {
            result.completeExceptionally(new CancelledException("the exchange was cancelled", null));
            exchange.cancel(true);
        });
        exchange.whenComplete((response, failure) -> {
            if (failure == null) {
                result.complete(new HttpResponse(response.statusCode(), response.body(), headers(response)));
            } else {
                result.completeExceptionally(translate(failure, cancellation));
            }
        });
        result.whenComplete((response, failure) -> {
            inFlight.remove(result);
            registration.release();
            if (failure != null) {
                // an aborted or cancelled exchange must not keep its socket
                exchange.cancel(true);
            }
        });
        return result;
    }

    @Override
    public CompletionStage<Void> close() {
        closed.set(true);
        final CompletableFuture<Void> done = new CompletableFuture<>();
        Thread.ofVirtual().name("hiero-http-close").start(() -> {
            // shuts the client down and waits until every in-flight exchange has finished
            client.close();
            done.complete(null);
        });
        return done;
    }

    @Override
    public CompletionStage<Void> close(final Duration closeTimeout) {
        Objects.requireNonNull(closeTimeout, "closeTimeout must not be null");
        closed.set(true);
        final CompletableFuture<Void> done = new CompletableFuture<>();
        Thread.ofVirtual().name("hiero-http-close").start(() -> {
            client.shutdown();
            boolean terminated;
            try {
                terminated = client.awaitTermination(closeTimeout);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                terminated = false;
            }
            if (!terminated) {
                for (final CompletableFuture<HttpResponse> exchange : Set.copyOf(inFlight)) {
                    exchange.completeExceptionally(
                            new ClientClosedException("the exchange was aborted when the client was closed", null));
                }
                client.shutdownNow();
            }
            done.complete(null);
        });
        return done;
    }

    private java.net.http.HttpRequest toNativeRequest(final HttpRequest request) {
        final Duration timeout = request.timeout() != null ? request.timeout()
                : configuration().defaultRequestTimeout();
        final byte[] body = request.body();
        final java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder(URI.create(request.url()))
                .timeout(timeout)
                .method(request.method().name(), body == null
                        ? java.net.http.HttpRequest.BodyPublishers.noBody()
                        : java.net.http.HttpRequest.BodyPublishers.ofByteArray(body));
        // header names are case-insensitive: a header of the request wins over a default header of the same name
        final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.putAll(configuration().defaultHeaders());
        headers.keySet().removeAll(request.headers().keySet());
        headers.putAll(request.headers());
        headers.forEach(builder::header);
        return builder.build();
    }

    private static Map<String, String> headers(final java.net.http.HttpResponse<byte[]> response) {
        final Map<String, String> headers = new TreeMap<>();
        response.headers().map().forEach((name, values) -> {
            // the pseudo header ":status" of HTTP/2 is no header
            if (!name.startsWith(":")) {
                headers.put(name, String.join(", ", values));
            }
        });
        return headers;
    }

    private Throwable translate(final Throwable failure, final Cancellation cancellation) {
        final Throwable cause = unwrap(failure);
        if (cancellation.isCancelled() || cause instanceof CancellationException) {
            return new CancelledException("the exchange was cancelled", cause);
        }
        if (cause instanceof HttpConnectTimeoutException) {
            return timeout("the connection could not be established within the connect timeout", cause);
        }
        if (cause instanceof HttpTimeoutException) {
            return timeout("the request timed out", cause);
        }
        if (closed.get()) {
            return new ClientClosedException("the HTTP client has been closed", cause);
        }
        return new ConnectionException("the exchange could not be carried out: " + cause, cause);
    }

    private static TimeoutException timeout(final String message, final Throwable cause) {
        final TimeoutException exception = new TimeoutException(message);
        exception.initCause(cause);
        return exception;
    }

    private static Throwable unwrap(final Throwable failure) {
        Throwable current = failure;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
