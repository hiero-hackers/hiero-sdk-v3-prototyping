package org.hiero.http;

import java.util.Objects;
import org.hiero.http.internal.DefaultHttpClient;

/// Factory methods of the package `org.hiero.http`.
public final class HttpFactory {

    /// Creates the default HttpClient of the SDK, wrapping the language's native HTTP stack.
    public static HttpClient createHttpClient(final HttpConfiguration configuration) {
        return new DefaultHttpClient(Objects.requireNonNull(configuration, "configuration must not be null"));
    }

    private HttpFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
