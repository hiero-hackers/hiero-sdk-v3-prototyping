package org.hiero.http;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `HttpConfiguration`, generated from the specs.
@DisplayName("HttpConfiguration")
class HttpConfigurationTest {

    @Test
    @DisplayName("creates a HttpConfiguration and returns the values")
    @Tag("testId:http.HttpConfiguration/create")
    void shouldCreateAndReturnTheValues() {
        final Map<String, String> defaultHeaders = defaultHeadersValue();
        final Duration connectTimeout = connectTimeoutValue();
        final Duration defaultRequestTimeout = defaultRequestTimeoutValue();
        final HttpConfiguration subject = new HttpConfiguration(defaultHeaders, connectTimeout, defaultRequestTimeout);
        assertValue(defaultHeaders, subject.defaultHeaders());
        assertValue(connectTimeout, subject.connectTimeout());
        assertValue(defaultRequestTimeout, subject.defaultRequestTimeout());
    }

    @Test
    @DisplayName("rejects null for defaultHeaders")
    @Tag("testId:http.HttpConfiguration/defaultHeaders/rejects-null")
    void shouldRejectNullDefaultHeaders() {
        assertThrows(NullPointerException.class, () -> new HttpConfiguration((Map<String, String>) null, connectTimeoutValue(), defaultRequestTimeoutValue()));
    }

    @Test
    @DisplayName("the constructor copies defaultHeaders; the accessor returns an unmodifiable map")
    @Tag("testId:http.HttpConfiguration/defaultHeaders/copies-in")
    void shouldCopyDefaultHeadersIn() {
        final Map<String, String> value = new LinkedHashMap<>(defaultHeadersValue());
        final Map<String, String> expected = Map.copyOf(value);
        final HttpConfiguration subject = new HttpConfiguration(value, connectTimeoutValue(), defaultRequestTimeoutValue());
        value.clear();
        assertValue(expected, subject.defaultHeaders());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.defaultHeaders()).clear());
    }

    @Test
    @DisplayName("rejects null for connectTimeout")
    @Tag("testId:http.HttpConfiguration/connectTimeout/rejects-null")
    void shouldRejectNullConnectTimeout() {
        assertThrows(NullPointerException.class, () -> new HttpConfiguration(defaultHeadersValue(), (Duration) null, defaultRequestTimeoutValue()));
    }

    @Test
    @DisplayName("rejects null for defaultRequestTimeout")
    @Tag("testId:http.HttpConfiguration/defaultRequestTimeout/rejects-null")
    void shouldRejectNullDefaultRequestTimeout() {
        assertThrows(NullPointerException.class, () -> new HttpConfiguration(defaultHeadersValue(), connectTimeoutValue(), (Duration) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:http.HttpConfiguration/value-semantics")
    void shouldCompareByValue() {
        final HttpConfiguration subject = create();
        final HttpConfiguration other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Map<String, String> defaultHeadersValue() {
        return Map.ofEntries(Map.entry("value", "value"));
    }

    private static Duration connectTimeoutValue() {
        return Duration.ofSeconds(1L);
    }

    private static Duration defaultRequestTimeoutValue() {
        return Duration.ofSeconds(1L);
    }

    private static HttpConfiguration create() {
        return new HttpConfiguration(defaultHeadersValue(), connectTimeoutValue(), defaultRequestTimeoutValue());
    }
}
