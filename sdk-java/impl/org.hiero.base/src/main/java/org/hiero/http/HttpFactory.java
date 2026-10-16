package org.hiero.http;


/// Factory methods of the package `org.hiero.http`.
public final class HttpFactory {

    /// Creates the default HttpClient of the SDK, wrapping the language's native HTTP stack.
    public static HttpClient createHttpClient(final HttpConfiguration configuration) {
        throw new UnsupportedOperationException("Not implemented yet: HttpFactory.createHttpClient");
    }

    private HttpFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
