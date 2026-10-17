package org.hiero.mirrornode.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.hiero.common.Page;
import org.hiero.common.PaginationException;
import org.hiero.http.HttpConfiguration;
import org.hiero.http.HttpFactory;
import org.hiero.http.HttpMethod;
import org.hiero.http.HttpResponse;
import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.MirrorNodeException;
import org.hiero.mirrornode.http.MirrorNodeHttpClient;
import org.hiero.mirrornode.http.MirrorNodeHttpRequest;
import org.hiero.mirrornode.internal.Json.JsonObject;
import org.jspecify.annotations.Nullable;

/// The calls of the repositories against the Mirror Node REST API: JSON requests through a `MirrorNodeHttpClient`,
/// the interpretation of the status code (`404` is an absent result, every other non-2xx status a
/// `MirrorNodeException`) and the pagination along `links.next`.
public final class MirrorNodeRest {

    /// The configuration of the `HttpClient` that the factories create for a mirror node.
    public static final HttpConfiguration DEFAULT_CONFIGURATION = new HttpConfiguration(
            Map.of("accept", "application/json"), Duration.ofSeconds(10), Duration.ofSeconds(30));

    private static final String API_PREFIX = "/api/v1";
    private static final int NOT_FOUND = 404;
    private static final int MAX_BODY_IN_MESSAGE = 500;

    private final MirrorNodeHttpClient client;

    /// Creates the calls through `client`.
    ///
    /// @param client the client of the mirror node
    /// @throws NullPointerException if `client` is `null`
    public MirrorNodeRest(final MirrorNodeHttpClient client) {
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    /// Creates the calls against `mirrorNode` through a new default `HttpClient`.
    ///
    /// @param mirrorNode the mirror node
    /// @return the calls
    /// @throws NullPointerException if `mirrorNode` is `null`
    public static MirrorNodeRest create(final MirrorNode mirrorNode) {
        Objects.requireNonNull(mirrorNode, "mirrorNode must not be null");
        return new MirrorNodeRest(createHttpClient(mirrorNode));
    }

    /// Creates a `MirrorNodeHttpClient` for `mirrorNode` with a new default `HttpClient`.
    ///
    /// @param mirrorNode the mirror node
    /// @return the client
    public static MirrorNodeHttpClient createHttpClient(final MirrorNode mirrorNode) {
        return new DefaultMirrorNodeHttpClient(mirrorNode, HttpFactory.createHttpClient(DEFAULT_CONFIGURATION));
    }

    /// Returns a stage that fails with a `MirrorNodeException` for a repository that is bound to no mirror node.
    ///
    /// @param repository the name of the repository type
    /// @param <T> the type of the result
    /// @return the failed stage
    public static <T> CompletionStage<T> unbound(final String repository) {
        return CompletableFuture.failedFuture(new MirrorNodeException(repository
                + " is bound to no mirror node; create it with the createRepository factory method", null));
    }

    /// Sends a GET request for `path` and parses the JSON object of the response; a `404` yields `null`.
    ///
    /// @param path the path relative to the REST base URL
    /// @return the object, or `null` if the mirror node does not know the requested entity
    public CompletionStage<@Nullable JsonObject> get(final String path) {
        return send(new MirrorNodeHttpRequest(HttpMethod.GET, path, null, null, Map.of()), path);
    }

    /// Sends a GET request for `path` and maps the JSON object of the response; a `404` yields `null`.
    ///
    /// @param path the path relative to the REST base URL
    /// @param mapper the mapping of the JSON object; it may return `null`
    /// @param <T> the type of the result
    /// @return the result, or `null`
    public <T> CompletionStage<@Nullable T> getAndMap(final String path,
            final Function<JsonObject, @Nullable T> mapper) {
        return get(path).thenApply(json -> json == null ? null : map(path, json, mapper));
    }

    /// Sends a POST request with `body` of the content type `contentType` and parses the JSON object of the response.
    ///
    /// @param path the path relative to the REST base URL
    /// @param body the body
    /// @param contentType the content type of the body
    /// @return the object
    public CompletionStage<JsonObject> post(final String path, final byte[] body, final String contentType) {
        final MirrorNodeHttpRequest request = new MirrorNodeHttpRequest(HttpMethod.POST, path, body, null,
                Map.of("content-type", contentType));
        return send(request, path).thenApply(json -> {
            if (json == null) {
                throw new MirrorNodeException("the mirror node does not provide " + path + " (status 404)", null);
            }
            return json;
        });
    }

    /// Loads the first page of the list endpoint `path`, whose items are the objects of the array `arrayName`.
    ///
    /// @param path the path relative to the REST base URL
    /// @param arrayName the name of the array of the items in the response
    /// @param mapper the mapping of an item; an item mapped to `null` is left out
    /// @param <T> the type of the items
    /// @return the first page
    public <T> CompletionStage<Page<T>> page(final String path, final String arrayName,
            final Function<JsonObject, @Nullable T> mapper) {
        return pageAsync(path, arrayName, json -> CompletableFuture.completedFuture(mapper.apply(json)));
    }

    /// Like `page`, but the mapping of an item is itself asynchronous (it may load more data).
    ///
    /// @param path the path relative to the REST base URL
    /// @param arrayName the name of the array of the items in the response
    /// @param mapper the asynchronous mapping of an item; an item mapped to `null` is left out
    /// @param <T> the type of the items
    /// @return the first page
    public <T> CompletionStage<Page<T>> pageAsync(final String path, final String arrayName,
            final Function<JsonObject, CompletionStage<@Nullable T>> mapper) {
        return loadPage(new PageQuery<>(this, path, arrayName, mapper), path, 0);
    }

    private <T> CompletionStage<Page<T>> loadPage(final PageQuery<T> query, final String path, final int pageIndex) {
        return get(path).thenCompose(json -> {
            if (json == null) {
                // the mirror node does not know the entity whose items are listed: there are none
                return CompletableFuture.completedFuture(new RestPage<>(query, List.of(), pageIndex, null));
            }
            final List<CompletableFuture<@Nullable T>> items = new ArrayList<>();
            final String next;
            try {
                for (final JsonObject item : json.objects(query.arrayName())) {
                    items.add(query.mapper().apply(item).toCompletableFuture());
                }
                final JsonObject links = json.optObject("links");
                next = links == null ? null : nextPath(links.optString("next"));
            } catch (final RuntimeException e) {
                throw unexpected(path, e);
            }
            return CompletableFuture.allOf(items.toArray(CompletableFuture<?>[]::new)).thenApply(ignored -> {
                final List<T> data = new ArrayList<>();
                for (final CompletableFuture<@Nullable T> item : items) {
                    final T value = item.join();
                    if (value != null) {
                        data.add(value);
                    }
                }
                return new RestPage<>(query, data, pageIndex, next);
            });
        });
    }

    /// Turns the `links.next` value of a response into a path relative to the REST base URL: the mirror node returns
    /// it as `/api/v1/...`, so the prefix is stripped exactly once. An absolute URL is reduced to its path and query.
    ///
    /// @param next the `links.next` value
    /// @return the path, or `null` if there is no next page
    static @Nullable String nextPath(final @Nullable String next) {
        if (next == null || next.isBlank()) {
            return null;
        }
        String path = next;
        if (!path.startsWith("/")) {
            try {
                final URI uri = new URI(next);
                path = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException("links.next is no path: " + next, e);
            }
        }
        if (path.startsWith(API_PREFIX + "/") || path.startsWith(API_PREFIX + "?")) {
            path = path.substring(API_PREFIX.length());
        }
        return path;
    }

    private CompletionStage<@Nullable JsonObject> send(final MirrorNodeHttpRequest request, final String path) {
        final CompletionStage<HttpResponse> exchange;
        try {
            exchange = client.execute(request);
        } catch (final RuntimeException e) {
            return CompletableFuture.failedFuture(failed(path, e));
        }
        return exchange.handle((response, failure) -> {
            if (failure != null) {
                throw failed(path, failure);
            }
            if (response.statusCode() == NOT_FOUND) {
                return null;
            }
            final String body = new String(response.body(), StandardCharsets.UTF_8);
            if (response.statusCode() < 200 || response.statusCode() > 299) {
                throw new MirrorNodeException("the mirror node answered " + request.method() + " " + path
                        + " with status " + response.statusCode() + ": " + abbreviate(body), null);
            }
            try {
                return Json.parseObject(body);
            } catch (final IllegalArgumentException e) {
                throw unexpected(path, e);
            }
        });
    }

    private static <T> @Nullable T map(final String path, final JsonObject json,
            final Function<JsonObject, @Nullable T> mapper) {
        try {
            return mapper.apply(json);
        } catch (final RuntimeException e) {
            throw unexpected(path, e);
        }
    }

    private static MirrorNodeException unexpected(final String path, final Throwable cause) {
        return new MirrorNodeException("unexpected response of the mirror node for " + path + ": "
                + cause.getMessage(), cause);
    }

    private static MirrorNodeException failed(final String path, final Throwable failure) {
        final Throwable cause = unwrap(failure);
        if (cause instanceof MirrorNodeException exception) {
            return exception;
        }
        return new MirrorNodeException("the request " + path + " to the mirror node failed: " + cause, cause);
    }

    /// Removes the `CompletionException` and `ExecutionException` wrappers of `failure`.
    ///
    /// @param failure the failure
    /// @return the cause
    static Throwable unwrap(final Throwable failure) {
        Throwable current = failure;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static String abbreviate(final String body) {
        return body.length() <= MAX_BODY_IN_MESSAGE ? body : body.substring(0, MAX_BODY_IN_MESSAGE) + "...";
    }

    /// A list query: what is needed to load any of its pages.
    ///
    /// @param rest the calls
    /// @param firstPath the path of the first page
    /// @param arrayName the name of the array of the items
    /// @param mapper the mapping of an item
    /// @param <T> the type of the items
    private record PageQuery<T>(MirrorNodeRest rest, String firstPath, String arrayName,
            Function<JsonObject, CompletionStage<@Nullable T>> mapper) {
    }

    /// One page of a list query; `next()` follows `links.next` of the response.
    ///
    /// @param <T> the type of the items
    private static final class RestPage<T> extends Page<T> {

        private final PageQuery<T> query;
        private final @Nullable String nextPath;

        RestPage(final PageQuery<T> query, final List<T> data, final int pageIndex, final @Nullable String nextPath) {
            super(data, data.size(), pageIndex);
            this.query = query;
            this.nextPath = nextPath;
        }

        @Override
        public boolean hasNext() {
            return nextPath != null;
        }

        @Override
        public boolean isFirst() {
            return pageIndex() == 0;
        }

        @Override
        public CompletionStage<Page<T>> next() {
            if (nextPath == null) {
                return CompletableFuture.failedFuture(new PaginationException("there is no next page", null));
            }
            return paginate(nextPath, pageIndex() + 1);
        }

        @Override
        public CompletionStage<Page<T>> first() {
            return paginate(query.firstPath(), 0);
        }

        private CompletionStage<Page<T>> paginate(final String path, final int index) {
            final CompletionStage<Page<T>> load;
            try {
                load = query.rest().loadPage(query, path, index);
            } catch (final RuntimeException e) {
                return CompletableFuture.failedFuture(new PaginationException("the page could not be loaded", e));
            }
            return load.handle((page, failure) -> {
                if (failure != null) {
                    final Throwable cause = unwrap(failure);
                    throw new PaginationException("the page could not be loaded: " + cause.getMessage(), cause);
                }
                return page;
            });
        }
    }
}
