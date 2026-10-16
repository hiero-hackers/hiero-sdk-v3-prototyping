package org.hiero.http;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `http`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("http")
class HttpSpecTest {

    /// `http/the-cancellation-that-never-fires-is-not-cancelled`
    ///
    /// Source: this spec (Cancellation.none() is the cancellation that never fires).
    @Test
    @DisplayName("the cancellation that never fires is not cancelled")
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
}
