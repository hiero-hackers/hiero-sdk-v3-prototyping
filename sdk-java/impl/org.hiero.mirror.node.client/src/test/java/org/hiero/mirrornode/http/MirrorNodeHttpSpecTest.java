package org.hiero.mirrornode.http;

import static org.hiero.sdk.testing.Testing.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.hiero.http.HttpMethod;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-http`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-http")
class MirrorNodeHttpSpecTest {

    /// Paths relative to the REST base URL: those that the Go SDK accepts (TestUnitMirrorRestPathAcceptsRelativePaths), the
    /// ".." inside a query string that the Java SDK accepts (MirrorNodeRestPathTest.allowsDotDotInsideQueryString) and the
    /// path of this spec's example.
    private static List<String> RELATIVE_PATHS() {
        return List.of("/accounts/0.0.1", "/accounts/0.0.1?limit=25", "/accounts/0.0.1/tokens?token.id=gt:0.0.5&order=asc", "/", "/accounts/0x00000000000000000000000000000000000004d2", "/accounts?order=..", "/accounts/0.0.1234?limit=25");
    }

    /// `mirrornode.http/a-request-keeps-a-path-relative-to-the-rest-base-url-unchanged`
    ///
    /// Source: the Go SDK (TestUnitMirrorRestPathAcceptsRelativePaths), the Java SDK
    /// (MirrorNodeRestPathTest.allowsDotDotInsideQueryString) and this spec's example; the values of RELATIVE_PATHS.
    @Test
    @DisplayName("a request keeps a path relative to the REST base URL unchanged")
    void aRequestKeepsAPathRelativeToTheRESTBaseURLUnchanged() {
        for (final String path : RELATIVE_PATHS()) {
            // when
            final MirrorNodeHttpRequest request = new MirrorNodeHttpRequest(HttpMethod.GET, path, null, null, Map.of());

            // then
            assertEquals(path, request.path());
            assertEquals(HttpMethod.GET, request.method());
            assertNull(request.body());
            assertNull(request.timeout());
            assertEquals(0L, length(request.headers()));
        }
    }

    /// `mirrornode.http/a-post-request-keeps-its-body-and-headers`
    ///
    /// Source: the Java SDK (MirrorNodeHttpClientTest: the POST body and the caller's headers are passed on unchanged).
    @Test
    @DisplayName("a POST request keeps its body and headers")
    void aPOSTRequestKeepsItsBodyAndHeaders() {
        // given
        final byte[] body = utf8("{\"data\":\"0xabc\"}");

        // when
        final MirrorNodeHttpRequest request = new MirrorNodeHttpRequest(HttpMethod.POST, "/contracts/call", body, null, Map.of("authorization", "Bearer test"));

        // then
        assertEquals(HttpMethod.POST, request.method());
        assertEquals(body, request.body());
        assertEquals(Map.of("authorization", "Bearer test"), request.headers());
    }

    /// `mirrornode.http/a-request-keeps-its-timeout`
    ///
    /// Source: the Java SDK (MirrorNodeHttpClientTest: calls with a deadline of 10 seconds); this spec (`timeout` is
    /// optional, a request without one falls back to the default request timeout of the HttpClient).
    @Test
    @DisplayName("a request keeps its timeout")
    void aRequestKeepsItsTimeout() {
        // when
        final MirrorNodeHttpRequest request = new MirrorNodeHttpRequest(HttpMethod.GET, "/accounts/0.0.1", null, toDuration("PT10S"), Map.of());

        // then
        assertEquals(toDuration("PT10S"), request.timeout());
        assertNotEquals(toDuration("PT10.001S"), request.timeout());
    }

    /// `mirrornode.http/a-request-keeps-a-timeout-with-millisecond-precision`
    ///
    /// Source: the API guideline (`duration` has millisecond precision).
    @Test
    @DisplayName("a request keeps a timeout with millisecond precision")
    void aRequestKeepsATimeoutWithMillisecondPrecision() {
        // when
        final MirrorNodeHttpRequest request = new MirrorNodeHttpRequest(HttpMethod.GET, "/accounts/0.0.1", null, toDuration("PT1.5S"), Map.of());
        final @Nullable Duration timeout = request.timeout();

        // then
        assertEquals(toDuration("PT1.5S"), timeout);
        assertNotNull(timeout);
        assertTrue(timeout.compareTo(toDuration("PT1.501S")) < 0);
        assertTrue(timeout.compareTo(toDuration("PT1.499S")) > 0);
    }
}
