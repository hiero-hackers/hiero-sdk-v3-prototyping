package org.hiero.mirrornode.http;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.hiero.http.HttpMethod;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `MirrorNodeHttpRequest`, generated from the specs.
@DisplayName("MirrorNodeHttpRequest")
class MirrorNodeHttpRequestTest {

    @Test
    @DisplayName("creates a MirrorNodeHttpRequest and returns the values")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/create")
    void shouldCreateAndReturnTheValues() {
        final HttpMethod method = methodValue();
        final String path = pathValue();
        final byte @Nullable [] body = bodyValue();
        final @Nullable Duration timeout = timeoutValue();
        final Map<String, String> headers = headersValue();
        final MirrorNodeHttpRequest subject = new MirrorNodeHttpRequest(method, path, body, timeout, headers);
        assertValue(method, subject.method());
        assertValue(path, subject.path());
        assertValue(body, subject.body());
        assertValue(timeout, subject.timeout());
        assertValue(headers, subject.headers());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final HttpMethod method = methodValue();
        final String path = pathValue();
        final byte @Nullable [] body = bodyValue();
        final @Nullable Duration timeout = timeoutValue();
        final Map<String, String> headers = headersValue();
        final MirrorNodeHttpRequest subject = MirrorNodeHttpRequest.builder()
                .method(method)
                .path(path)
                .body(body)
                .timeout(timeout)
                .headers(headers)
                .build();
        assertValue(method, subject.method());
        assertValue(path, subject.path());
        assertValue(body, subject.body());
        assertValue(timeout, subject.timeout());
        assertValue(headers, subject.headers());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final MirrorNodeHttpRequest subject = MirrorNodeHttpRequest.builder()
                .method(methodValue())
                .path(pathValue())
                .headers(headersValue())
                .build();
        assertNull(subject.body());
        assertNull(subject.timeout());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> MirrorNodeHttpRequest.builder().build());
    }

    @Test
    @DisplayName("rejects null for method")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/method/rejects-null")
    void shouldRejectNullMethod() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeHttpRequest((HttpMethod) null, pathValue(), bodyValue(), timeoutValue(), headersValue()));
    }

    @Test
    @DisplayName("rejects null for path")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/path/rejects-null")
    void shouldRejectNullPath() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeHttpRequest(methodValue(), (String) null, bodyValue(), timeoutValue(), headersValue()));
    }

    @Test
    @DisplayName("path: rejects a value that does not match the pattern")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/path/rejects-not-matching-the-pattern")
    void shouldRejectPathNotMatchingThePattern() {
        assertThrows(IllegalArgumentException.class, () -> new MirrorNodeHttpRequest(methodValue(), "", bodyValue(), timeoutValue(), headersValue()));
    }

    @Test
    @DisplayName("the constructor copies body; the accessor returns a copy")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/body/copies-in")
    void shouldCopyBodyIn() {
        final byte[] value = bodyValue();
        final byte[] expected = value.clone();
        final MirrorNodeHttpRequest subject = new MirrorNodeHttpRequest(methodValue(), pathValue(), value, timeoutValue(), headersValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.body());
        final byte[] returned = subject.body();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.body());
    }

    @Test
    @DisplayName("rejects null for headers")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/headers/rejects-null")
    void shouldRejectNullHeaders() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeHttpRequest(methodValue(), pathValue(), bodyValue(), timeoutValue(), (Map<String, String>) null));
    }

    @Test
    @DisplayName("the constructor copies headers; the accessor returns an unmodifiable map")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/headers/copies-in")
    void shouldCopyHeadersIn() {
        final Map<String, String> value = new LinkedHashMap<>(headersValue());
        final Map<String, String> expected = Map.copyOf(value);
        final MirrorNodeHttpRequest subject = new MirrorNodeHttpRequest(methodValue(), pathValue(), bodyValue(), timeoutValue(), value);
        value.clear();
        assertValue(expected, subject.headers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.headers()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.http.MirrorNodeHttpRequest/value-semantics")
    void shouldCompareByValue() {
        final MirrorNodeHttpRequest subject = create();
        final MirrorNodeHttpRequest other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static HttpMethod methodValue() {
        return HttpMethod.GET;
    }

    private static String pathValue() {
        return "/";
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

    private static MirrorNodeHttpRequest create() {
        return new MirrorNodeHttpRequest(methodValue(), pathValue(), bodyValue(), timeoutValue(), headersValue());
    }
}
