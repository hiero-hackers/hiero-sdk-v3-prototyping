package org.hiero.http;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `http`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("http")
class HttpSpecTest {

    /// The configuration of the first example of this spec: JSON is accepted, 5 seconds to connect, 10 seconds per request.
    private static HttpConfiguration EXAMPLE_CONFIGURATION() {
        return new HttpConfiguration(Map.of("accept", "application/json"), toDuration("PT5S"), toDuration("PT10S"));
    }

    /// `http/the-cancellation-that-never-fires-is-not-cancelled`
    ///
    /// Source: this spec (Cancellation.none() is the cancellation that never fires).
    @Test
    @DisplayName("the cancellation that never fires is not cancelled")
    @Tag("testId:http/the-cancellation-that-never-fires-is-not-cancelled")
    void theCancellationThatNeverFiresIsNotCancelled() {
        // when
        final Cancellation cancellation = Cancellation.none();

        // then
        assertFalse(cancellation.isCancelled());
    }

    /// `http/a-new-cancellation-source-is-not-cancelled`
    ///
    /// Source: this spec (a CancellationSource owns the right to cancel; only its cancellation is handed out).
    @Test
    @DisplayName("a new cancellation source is not cancelled")
    @Tag("testId:http/a-new-cancellation-source-is-not-cancelled")
    void aNewCancellationSourceIsNotCancelled() {
        // when
        final CancellationSource source = CancellationSource.create();

        // then
        assertFalse(source.cancellation().isCancelled());
    }

    /// `http/cancelling-a-source-cancels-its-cancellation-also-when-cancelled-twice`
    ///
    /// Source: this spec (cancel() requests cancellation; it is idempotent, a second call does nothing).
    @Test
    @DisplayName("cancelling a source cancels its cancellation, also when cancelled twice")
    @Tag("testId:http/cancelling-a-source-cancels-its-cancellation-also-when-cancelled-twice")
    void cancellingASourceCancelsItsCancellationAlsoWhenCancelledTwice() {
        // given
        final CancellationSource source = CancellationSource.create();

        // when
        source.cancel();
        source.cancel();

        // then
        assertTrue(source.cancellation().isCancelled());
    }

    /// `http/cancelling-a-source-cancels-no-other-cancellation`
    ///
    /// Source: this spec (a source owns its own cancellation; cancelling one source cancels no other).
    @Test
    @DisplayName("cancelling a source cancels no other cancellation")
    @Tag("testId:http/cancelling-a-source-cancels-no-other-cancellation")
    void cancellingASourceCancelsNoOtherCancellation() {
        // given
        final CancellationSource cancelled = CancellationSource.create();
        final CancellationSource other = CancellationSource.create();

        // when
        cancelled.cancel();

        // then
        assertFalse(other.cancellation().isCancelled());
        assertFalse(Cancellation.none().isCancelled());
    }

    /// `http/a-request-keeps-its-absolute-url`
    ///
    /// Source: this spec (@@urlPattern: url is an absolute, well-formed URL; the URLs of the examples of this spec and of the
    /// default mirror node of `ledger`).
    @Test
    @DisplayName("a request keeps its absolute URL")
    @Tag("testId:http/a-request-keeps-its-absolute-url")
    void aRequestKeepsItsAbsoluteURL() {
        for (final String url : List.of("https://mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234", "https://example.org/api/v1/submit", "http://127.0.0.1:38081")) {
            // when
            final HttpRequest request = new HttpRequest(HttpMethod.GET, url, null, null, Map.of());

            // then
            assertEquals(url, request.url());
            assertEquals(HttpMethod.GET, request.method());
            assertNull(request.body());
            assertNull(request.timeout());
        }
    }

    /// `http/an-http-configuration-keeps-its-timeouts-and-default-headers`
    ///
    /// Source: this spec (the configuration of the first example: connectTimeout 5s, defaultRequestTimeout 10s and an
    /// "accept" default header).
    @Test
    @DisplayName("an HTTP configuration keeps its timeouts and default headers")
    @Tag("testId:http/an-http-configuration-keeps-its-timeouts-and-default-headers")
    void anHTTPConfigurationKeepsItsTimeoutsAndDefaultHeaders() {
        // when
        final HttpConfiguration configuration = new HttpConfiguration(Map.of("accept", "application/json"), toDuration("PT5S"), toDuration("PT10S"));

        // then
        assertEquals(toDuration("PT5S"), configuration.connectTimeout());
        assertEquals(toDuration("PT10S"), configuration.defaultRequestTimeout());
        assertEquals(Map.of("accept", "application/json"), configuration.defaultHeaders());
    }

    /// `http/a-request-keeps-its-own-timeout-and-headers`
    ///
    /// Source: this spec (the second example: a POST request with its own timeout of 30s and its own headers). The body is
    /// a stand-in for the JSON payload of the example.
    @Test
    @DisplayName("a request keeps its own timeout and headers")
    @Tag("testId:http/a-request-keeps-its-own-timeout-and-headers")
    void aRequestKeepsItsOwnTimeoutAndHeaders() {
        // given
        final byte[] body = utf8("{}");

        // when
        final HttpRequest request = new HttpRequest(HttpMethod.POST, "https://example.org/api/v1/submit", body, toDuration("PT30S"), Map.of("content-type", "application/json"));

        // then
        assertEquals(toDuration("PT30S"), request.timeout());
        assertEquals(Map.of("content-type", "application/json"), request.headers());
        assertEquals(body, request.body());
    }

    /// `http/a-request-with-a-relative-url-is-rejected`
    ///
    /// Source: this spec (@@urlPattern: url is absolute; no base-URL resolution happens here). The path of the first
    /// example without scheme and host.
    @Test
    @DisplayName("a request with a relative URL is rejected")
    @Tag("testId:http/a-request-with-a-relative-url-is-rejected")
    void aRequestWithARelativeURLIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(HttpMethod.GET, "/api/v1/accounts/0.0.1234", null, null, Map.of()));
    }

    /// `http/a-request-with-a-url-without-scheme-is-rejected`
    ///
    /// Source: this spec (@@urlPattern: an absolute URL has a scheme). The URL of the first example without its scheme.
    @Test
    @DisplayName("a request with a URL without scheme is rejected")
    @Tag("testId:http/a-request-with-a-url-without-scheme-is-rejected")
    void aRequestWithAURLWithoutSchemeIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(HttpMethod.GET, "mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234", null, null, Map.of()));
    }

    /// `http/the-default-client-applies-the-configuration-it-was-created-with`
    ///
    /// Source: this spec (an HttpClient carries the configuration it applies to every exchange; createHttpClient creates
    /// the default client for a configuration).
    @Test
    @DisplayName("the default client applies the configuration it was created with")
    @Tag("testId:http/the-default-client-applies-the-configuration-it-was-created-with")
    void theDefaultClientAppliesTheConfigurationItWasCreatedWith() {
        // when
        final HttpClient client = HttpFactory.createHttpClient(EXAMPLE_CONFIGURATION());

        // then
        assertEquals(EXAMPLE_CONFIGURATION(), client.configuration());
        client.close().toCompletableFuture().join();
    }

    /// `http/a-client-closed-with-a-timeout-rejects-a-request`
    ///
    /// Source: this spec (client-closed-error: the client was closed before the request was submitted; close(closeTimeout)
    /// completes normally and is idempotent like close(); the request and the 5s close timeout of the first example).
    @Test
    @DisplayName("a client closed with a timeout rejects a request")
    @Tag("testId:http/a-client-closed-with-a-timeout-rejects-a-request")
    void aClientClosedWithATimeoutRejectsARequest() throws Exception {
        // given
        final HttpClient client = HttpFactory.createHttpClient(EXAMPLE_CONFIGURATION());
        final HttpRequest request = new HttpRequest(HttpMethod.GET, "https://mainnet.mirrornode.hedera.com/api/v1/accounts/0.0.1234", null, null, Map.of());
        client.close(toDuration("PT5S")).toCompletableFuture().join();
        client.close(toDuration("PT5S")).toCompletableFuture().join();

        // when / then
        assertThrows(ClientClosedException.class, () -> {
            try {
                client.execute(request, Cancellation.none()).toCompletableFuture().join();
            } catch (final CompletionException failed) {
                throw failed.getCause();
            }
        });
    }
}
