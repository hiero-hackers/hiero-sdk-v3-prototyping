package org.hiero.mirrornode.http;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.hiero.http.HttpMethod;
import org.jspecify.annotations.Nullable;

/// @param path Path of the REST endpoint relative to the Mirror Node's REST base URL, including any query string
///   (e.g. "/accounts/0.0.1234?limit=25"). Must start with "/", must not contain whitespace and must not
///   repeat the "/api/v1" prefix; an absolute URL is rejected when the request is created.
public record MirrorNodeHttpRequest(
        HttpMethod method,
        String path,
        byte @Nullable [] body,
        @Nullable Duration timeout,
        Map<String, String> headers) {

    private static final Pattern PATH_PATTERN = Pattern.compile("^/[^\\s]*$");

    /// Creates a new `MirrorNodeHttpRequest`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public MirrorNodeHttpRequest {
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(path, "path must not be null");
        if (!PATH_PATTERN.matcher(path).find()) {
            throw new IllegalArgumentException("path must match the pattern ^/[^\\s]*$");
        }
        body = body == null ? null : body.clone();
        Objects.requireNonNull(headers, "headers must not be null");
        headers = Map.copyOf(headers);
    }

    /// Returns a copy of the `body` array.
    @Override
    public byte @Nullable [] body() {
        return body == null ? null : body.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof MirrorNodeHttpRequest other
            && Objects.equals(method, other.method)
            && Objects.equals(path, other.path)
            && Arrays.equals(body, other.body)
            && Objects.equals(timeout, other.timeout)
            && Objects.equals(headers, other.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path, Arrays.hashCode(body), timeout, headers);
    }

    @Override
    public String toString() {
        return "MirrorNodeHttpRequest[method=" + method + ", path=" + path + ", body=" + (body == null ? "null" : "byte[" + body.length + "]") + ", timeout=" + timeout + ", headers=" + headers + "]";
    }

    /// Returns a builder for a new `MirrorNodeHttpRequest`.
    ///
    /// @return the builder
    public static Builder builder() {
        return new Builder();
    }

    /// The builder of `MirrorNodeHttpRequest`.
    ///
    /// `build()` creates the `MirrorNodeHttpRequest` of the values set one by one.
    ///
    /// A value that is not set is `null`, or has its default value.
    public static final class Builder {

        private @Nullable HttpMethod method;
        private @Nullable String path;
        private byte @Nullable [] body;
        private @Nullable Duration timeout;
        private @Nullable Map<String, String> headers;

        private Builder() {
            // created by MirrorNodeHttpRequest.builder()
        }

        /// Sets the `method`.
        ///
        /// @param method the value
        /// @return this builder
        public Builder method(final HttpMethod method) {
            this.method = method;
            return this;
        }

        /// Sets the `path`.
        ///
        /// Path of the REST endpoint relative to the Mirror Node's REST base URL, including any query string
        /// (e.g. "/accounts/0.0.1234?limit=25"). Must start with "/", must not contain whitespace and must not
        /// repeat the "/api/v1" prefix; an absolute URL is rejected when the request is created.
        ///
        /// @param path the value
        /// @return this builder
        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        /// Sets the `body`.
        ///
        /// @param body the value
        /// @return this builder
        public Builder body(final byte @Nullable [] body) {
            this.body = body;
            return this;
        }

        /// Sets the `timeout`.
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

        /// Creates the `MirrorNodeHttpRequest` of the values set.
        ///
        /// @return the new `MirrorNodeHttpRequest`
        /// @throws NullPointerException if a required value is not set
        /// @throws IllegalArgumentException if a value violates its constraints
        public MirrorNodeHttpRequest build() {
            return new MirrorNodeHttpRequest(
                    Objects.requireNonNull(method, "method must not be null"),
                    Objects.requireNonNull(path, "path must not be null"),
                    body,
                    timeout,
                    Objects.requireNonNull(headers, "headers must not be null"));
        }
    }
}
