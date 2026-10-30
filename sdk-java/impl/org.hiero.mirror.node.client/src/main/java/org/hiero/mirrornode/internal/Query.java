package org.hiero.mirrornode.internal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.hiero.mirrornode.common.Order;
import org.jspecify.annotations.Nullable;

/// Builds the path of a request with its query parameters, in the order they are added. A `null` value and an empty
/// list of conditions are left out; every value is URL-encoded.
public final class Query {

    private final StringBuilder path;
    private boolean first = true;

    private Query(final String path) {
        this.path = new StringBuilder(path);
    }

    /// Starts a query on `path`.
    ///
    /// @param path the path relative to the REST base URL, without a query string
    /// @return the query
    /// @throws NullPointerException if `path` is `null`
    public static Query of(final String path) {
        return new Query(Objects.requireNonNull(path, "path must not be null"));
    }

    /// Adds the parameter `name` with `value`, unless `value` is `null`. An `Order` is sent in lower case, any other
    /// enum by its name, everything else by its `toString()`.
    ///
    /// @param name the name of the parameter
    /// @param value the value, or `null`
    /// @return this query
    public Query add(final String name, final @Nullable Object value) {
        if (value == null) {
            return this;
        }
        final String text = switch (value) {
            case Order order -> order.name().toLowerCase(Locale.ROOT);
            case Enum<?> constant -> constant.name();
            default -> value.toString();
        };
        return append(name, text);
    }

    /// Adds the parameter `name` once per condition (a value with an optional operator prefix such as `gte:`).
    ///
    /// @param name the name of the parameter
    /// @param conditions the conditions; an empty list adds nothing
    /// @return this query
    /// @throws NullPointerException if `conditions` is `null`
    public Query addAll(final String name, final List<String> conditions) {
        Objects.requireNonNull(conditions, "conditions must not be null");
        conditions.forEach(condition -> append(name, condition));
        return this;
    }

    /// Returns the path with its query string.
    ///
    /// @return the path
    public String build() {
        return path.toString();
    }

    private Query append(final String name, final String value) {
        path.append(first ? '?' : '&').append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        first = false;
        return this;
    }

    @Override
    public String toString() {
        return build();
    }
}
