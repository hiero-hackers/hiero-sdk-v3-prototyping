package org.hiero.http;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `HttpRequest`, generated from the specs.
@DisplayName("HttpRequest")
class HttpRequestTest {

    @Test
    @DisplayName("creates a HttpRequest and returns the values")
    @Tag("testId:http.HttpRequest/create")
    void shouldCreateAndReturnTheValues() {
        final HttpMethod method = methodValue();
        final String url = urlValue();
        final byte @Nullable [] body = bodyValue();
        final @Nullable Duration timeout = timeoutValue();
        final Map<String, String> headers = headersValue();
        final HttpRequest subject = new HttpRequest(method, url, body, timeout, headers);
        assertValue(method, subject.method());
        assertValue(url, subject.url());
        assertValue(body, subject.body());
        assertValue(timeout, subject.timeout());
        assertValue(headers, subject.headers());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:http.HttpRequest/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final HttpMethod method = methodValue();
        final String url = urlValue();
        final byte @Nullable [] body = bodyValue();
        final @Nullable Duration timeout = timeoutValue();
        final Map<String, String> headers = headersValue();
        final HttpRequest subject = HttpRequest.builder()
                .method(method)
                .url(url)
                .body(body)
                .timeout(timeout)
                .headers(headers)
                .build();
        assertValue(method, subject.method());
        assertValue(url, subject.url());
        assertValue(body, subject.body());
        assertValue(timeout, subject.timeout());
        assertValue(headers, subject.headers());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:http.HttpRequest/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final HttpRequest subject = HttpRequest.builder()
                .method(methodValue())
                .url(urlValue())
                .headers(headersValue())
                .build();
        assertNull(subject.body());
        assertNull(subject.timeout());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:http.HttpRequest/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> HttpRequest.builder().build());
    }

    @Test
    @DisplayName("rejects null for method")
    @Tag("testId:http.HttpRequest/method/rejects-null")
    void shouldRejectNullMethod() {
        assertThrows(NullPointerException.class, () -> new HttpRequest((HttpMethod) null, urlValue(), bodyValue(), timeoutValue(), headersValue()));
    }

    @Test
    @DisplayName("rejects null for url")
    @Tag("testId:http.HttpRequest/url/rejects-null")
    void shouldRejectNullUrl() {
        assertThrows(NullPointerException.class, () -> new HttpRequest(methodValue(), (String) null, bodyValue(), timeoutValue(), headersValue()));
    }

    @Test
    @DisplayName("url: rejects a value that is no URL")
    @Tag("testId:http.HttpRequest/url/rejects-that-is-no-url")
    void shouldRejectUrlThatIsNoUrl() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(methodValue(), "not a url", bodyValue(), timeoutValue(), headersValue()));
    }

    @Test
    @DisplayName("the constructor copies body; the accessor returns a copy")
    @Tag("testId:http.HttpRequest/body/copies-in")
    void shouldCopyBodyIn() {
        final byte[] value = bodyValue();
        final byte[] expected = value.clone();
        final HttpRequest subject = new HttpRequest(methodValue(), urlValue(), value, timeoutValue(), headersValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.body());
        final byte[] returned = subject.body();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.body());
    }

    @Test
    @DisplayName("rejects null for headers")
    @Tag("testId:http.HttpRequest/headers/rejects-null")
    void shouldRejectNullHeaders() {
        assertThrows(NullPointerException.class, () -> new HttpRequest(methodValue(), urlValue(), bodyValue(), timeoutValue(), (Map<String, String>) null));
    }

    @Test
    @DisplayName("the constructor copies headers; the accessor returns an unmodifiable map")
    @Tag("testId:http.HttpRequest/headers/copies-in")
    void shouldCopyHeadersIn() {
        final Map<String, String> value = new LinkedHashMap<>(headersValue());
        final Map<String, String> expected = Map.copyOf(value);
        final HttpRequest subject = new HttpRequest(methodValue(), urlValue(), bodyValue(), timeoutValue(), value);
        value.clear();
        assertValue(expected, subject.headers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.headers()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:http.HttpRequest/value-semantics")
    void shouldCompareByValue() {
        final HttpRequest subject = create();
        final HttpRequest other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static HttpMethod methodValue() {
        return HttpMethod.GET;
    }

    private static String urlValue() {
        return "https://example.com/v0";
    }

    private static byte[] bodyValue() {
        return new byte[] {1, 2, 3};
    }

    private static Duration timeoutValue() {
        return Duration.ofSeconds(1L);
    }

    private static Map<String, String> headersValue() {
        return Map.ofEntries(Map.entry("value", "value"));
    }

    private static HttpRequest create() {
        return new HttpRequest(methodValue(), urlValue(), bodyValue(), timeoutValue(), headersValue());
    }
}
