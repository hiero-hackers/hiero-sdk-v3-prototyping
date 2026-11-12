package org.hiero.consensusnode.clpr.queries;

import org.hiero.consensusnode.client.internal.Querying;
import org.hiero.consensusnode.clpr.internal.ClprQueries;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.queries.FreeQuery;
import org.hiero.consensusnode.queries.QueryResponse;
import org.jspecify.annotations.Nullable;

/// Free query that returns the CLPR endpoint manifest of the ledger. Has no input fields.
///
/// @see <a href="https://github.com/hiero-ledger/hiero-improvement-proposals/blob/main/HIP/hip-1535.md">HIP-1535</a>
public final class ClprEndpointManifestQuery extends FreeQuery<ClprEndpointManifest> {

    /// Creates a new `ClprEndpointManifestQuery`.
    public ClprEndpointManifestQuery() {
        super();
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public ClprEndpointManifestQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public ClprEndpointManifestQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up, and with
    /// `unknown-status-error` when a node answers with a status code that the SDK cannot name.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` or `UnknownStatusException` if the operation fails.
    @Override
    public CompletionStage<QueryResponse<ClprEndpointManifest>> submit(final HieroClient<?> client) {
        return Querying.free(this, this, ClprQueries.ENDPOINT_MANIFEST, client);
    }

    @Override
    public String toString() {
        return "ClprEndpointManifestQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + "]";
    }
}
