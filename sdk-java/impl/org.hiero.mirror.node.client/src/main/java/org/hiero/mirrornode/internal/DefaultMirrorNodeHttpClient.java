package org.hiero.mirrornode.internal;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import org.hiero.http.Cancellation;
import org.hiero.http.HttpClient;
import org.hiero.http.HttpRequest;
import org.hiero.http.HttpResponse;
import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.http.MirrorNodeHttpClient;
import org.hiero.mirrornode.http.MirrorNodeHttpRequest;

/// The `MirrorNodeHttpClient` of the SDK: resolves the path of a request against the REST base URL of its mirror node
/// by plain concatenation (a trailing slash of the base URL is ignored) and sends it with the wrapped `HttpClient`,
/// which it never closes.
public final class DefaultMirrorNodeHttpClient extends MirrorNodeHttpClient {

    /// Creates a client for `mirrorNode` that sends its requests with `httpClient`.
    ///
    /// @param mirrorNode the mirror node
    /// @param httpClient the transport client
    /// @throws NullPointerException if a value is `null`
    public DefaultMirrorNodeHttpClient(final MirrorNode mirrorNode, final HttpClient httpClient) {
        super(mirrorNode, httpClient);
    }

    @Override
    public CompletionStage<HttpResponse> execute(final MirrorNodeHttpRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        final HttpRequest httpRequest = new HttpRequest(request.method(), resolve(request.path()), request.body(),
                request.timeout(), request.headers());
        return httpClient().execute(httpRequest, Cancellation.none());
    }

    /// Resolves `path` against the REST base URL of the mirror node.
    ///
    /// @param path the path, starting with "/"
    /// @return the absolute URL
    public String resolve(final String path) {
        final String base = mirrorNode().restBaseUrl();
        return (base.endsWith("/") ? base.substring(0, base.length() - 1) : base) + path;
    }
}
