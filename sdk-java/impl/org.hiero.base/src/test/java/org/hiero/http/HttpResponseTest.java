package org.hiero.http;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `HttpResponse`, generated from the specs.
@DisplayName("HttpResponse")
class HttpResponseTest {

    @Test
    @DisplayName("creates a HttpResponse and returns the values")
    @Tag("testId:http.HttpResponse/create")
    void shouldCreateAndReturnTheValues() {
        final int statusCode = statusCodeValue();
        final byte[] body = bodyValue();
        final Map<String, String> headers = headersValue();
        final HttpResponse subject = new HttpResponse(statusCode, body, headers);
        assertValue(statusCode, subject.statusCode());
        assertValue(body, subject.body());
        assertValue(headers, subject.headers());
    }

    @Test
    @DisplayName("statusCode: accepts the minimum (0)")
    @Tag("testId:http.HttpResponse/statusCode/accepts-at-minimum")
    void shouldAcceptStatusCodeAtMinimum() {
        final int value = 0;
        final HttpResponse subject = new HttpResponse(value, bodyValue(), headersValue());
        assertValue(value, subject.statusCode());
    }

    @Test
    @DisplayName("statusCode: accepts the maximum (65535)")
    @Tag("testId:http.HttpResponse/statusCode/accepts-at-maximum")
    void shouldAcceptStatusCodeAtMaximum() {
        final int value = 65535;
        final HttpResponse subject = new HttpResponse(value, bodyValue(), headersValue());
        assertValue(value, subject.statusCode());
    }

    @Test
    @DisplayName("statusCode: rejects a value below the minimum (-1)")
    @Tag("testId:http.HttpResponse/statusCode/rejects-below-minimum")
    void shouldRejectStatusCodeBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new HttpResponse(-1, bodyValue(), headersValue()));
    }

    @Test
    @DisplayName("statusCode: rejects a value above the maximum (65536)")
    @Tag("testId:http.HttpResponse/statusCode/rejects-above-maximum")
    void shouldRejectStatusCodeAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new HttpResponse(65536, bodyValue(), headersValue()));
    }

    @Test
    @DisplayName("rejects null for body")
    @Tag("testId:http.HttpResponse/body/rejects-null")
    void shouldRejectNullBody() {
        assertThrows(NullPointerException.class, () -> new HttpResponse(statusCodeValue(), (byte[]) null, headersValue()));
    }

    @Test
    @DisplayName("the constructor copies body; the accessor returns a copy")
    @Tag("testId:http.HttpResponse/body/copies-in")
    void shouldCopyBodyIn() {
        final byte[] value = bodyValue();
        final byte[] expected = value.clone();
        final HttpResponse subject = new HttpResponse(statusCodeValue(), value, headersValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.body());
        final byte[] returned = subject.body();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.body());
    }

    @Test
    @DisplayName("rejects null for headers")
    @Tag("testId:http.HttpResponse/headers/rejects-null")
    void shouldRejectNullHeaders() {
        assertThrows(NullPointerException.class, () -> new HttpResponse(statusCodeValue(), bodyValue(), (Map<String, String>) null));
    }

    @Test
    @DisplayName("the constructor copies headers; the accessor returns an unmodifiable map")
    @Tag("testId:http.HttpResponse/headers/copies-in")
    void shouldCopyHeadersIn() {
        final Map<String, String> value = new LinkedHashMap<>(headersValue());
        final Map<String, String> expected = Map.copyOf(value);
        final HttpResponse subject = new HttpResponse(statusCodeValue(), bodyValue(), value);
        value.clear();
        assertValue(expected, subject.headers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.headers()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:http.HttpResponse/value-semantics")
    void shouldCompareByValue() {
        final HttpResponse subject = create();
        final HttpResponse other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static int statusCodeValue() {
        return 1;
    }

    private static byte[] bodyValue() {
        return new byte[] {1, 2, 3};
    }

    private static Map<String, String> headersValue() {
        return Map.ofEntries(Map.entry("value", "value"));
    }

    private static HttpResponse create() {
        return new HttpResponse(statusCodeValue(), bodyValue(), headersValue());
    }
}
