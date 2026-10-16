package org.hiero.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// A single HTTP request.
///
/// @param url Absolute, well-formed URL of the request. Checked when the request is created, so an
///   invalid URL fails there and never reaches `execute`. No base-URL resolution happens here.
/// @param body request body; absent for requests without a body
/// @param timeout request timeout; if absent, the configuration's defaultRequestTimeout applies
public record HttpRequest(
        HttpMethod method,
        String url,
        byte @Nullable [] body,
        @Nullable Duration timeout,
        Map<String, String> headers) {

    /// Creates a new `HttpRequest`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public HttpRequest {
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(url, "url must not be null");
        try {
            final URI urlUri = new URI(url);
            if (!urlUri.isAbsolute() || urlUri.getHost() == null) {
                throw new IllegalArgumentException("url must be an absolute URL with a host: " + url);
            }
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("url must be a valid URL: " + url, e);
        }
        body = body == null ? null : body.clone();
        Objects.requireNonNull(headers, "headers must not be null");
        headers = Map.copyOf(headers);
    }

    /// request body; absent for requests without a body
    ///
    /// Returns a copy of the `body` array.
    @Override
    public byte @Nullable [] body() {
        return body == null ? null : body.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof HttpRequest other
            && Objects.equals(method, other.method)
            && Objects.equals(url, other.url)
            && Arrays.equals(body, other.body)
            && Objects.equals(timeout, other.timeout)
            && Objects.equals(headers, other.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, url, Arrays.hashCode(body), timeout, headers);
    }

    @Override
    public String toString() {
        return "HttpRequest[method=" + method + ", url=" + url + ", body=" + (body == null ? "null" : "byte[" + body.length + "]") + ", timeout=" + timeout + ", headers=" + headers + "]";
    }

    /// Returns a builder for a new `HttpRequest`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `HttpRequest`.
    ///
    /// `build()` creates the `HttpRequest` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable HttpMethod method;
        private @Nullable String url;
        private byte @Nullable [] body;
        private @Nullable Duration timeout;
        private @Nullable Map<String, String> headers;

        private Builder() {
            // created by HttpRequest.builder()
        }

        /// Sets the `method`.
        ///
        /// @param method the value
        /// @return this builder
        public Builder method(final HttpMethod method) {
            this.method = method;
            return this;
        }

        /// Sets the `url`.
        ///
        /// Absolute, well-formed URL of the request. Checked when the request is created, so an
        /// invalid URL fails there and never reaches `execute`. No base-URL resolution happens here.
        ///
        /// @param url the value
        /// @return this builder
        public Builder url(final String url) {
            this.url = url;
            return this;
        }

        /// Sets the `body`.
        ///
        /// request body; absent for requests without a body
        ///
        /// @param body the value
        /// @return this builder
        public Builder body(final byte @Nullable [] body) {
            this.body = body;
            return this;
        }

        /// Sets the `timeout`.
        ///
        /// request timeout; if absent, the configuration's defaultRequestTimeout applies
        ///
        /// @param timeout the value
        /// @return this builder
        public Builder timeout(final @Nullable Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /// Sets the `headers`.
        ///
        /// @param headers the value
        /// @return this builder
        public Builder headers(final Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /// Creates the `HttpRequest` of the values set.
        ///
        /// @return the new `HttpRequest`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public HttpRequest build() {
            return new HttpRequest(
                    Objects.requireNonNull(method, "method must not be null"),
                    Objects.requireNonNull(url, "url must not be null"),
                    body,
                    timeout,
                    Objects.requireNonNull(headers, "headers must not be null"));
        }
    }
}
