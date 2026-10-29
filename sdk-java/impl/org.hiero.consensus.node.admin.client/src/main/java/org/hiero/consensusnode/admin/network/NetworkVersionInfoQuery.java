package org.hiero.consensusnode.admin.network;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.admin.internal.AdminQueries;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.internal.Querying;
import org.hiero.consensusnode.queries.FreeQuery;
import org.hiero.consensusnode.queries.QueryResponse;
import org.jspecify.annotations.Nullable;

/// Free query that returns the network's current HAPI and services version. Has no input
/// fields — the answer is per-network, not per-entity.
public final class NetworkVersionInfoQuery extends FreeQuery<NetworkVersionInfo> {

    /// Creates a new `NetworkVersionInfoQuery`.
    public NetworkVersionInfoQuery() {
        super();
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public NetworkVersionInfoQuery setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public NetworkVersionInfoQuery setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` if the operation fails.
    @Override
    public CompletionStage<QueryResponse<NetworkVersionInfo>> submit(final HieroClient<?> client) {
        // The network bills NetworkGetVersionInfo like any other paid query: without a payment it is rejected at
        // precheck. The answer is therefore bought with a payment of the quoted cost from the operator.
        return Querying.paid(this, this, null, AdminQueries.NETWORK_VERSION_INFO, client)
                .<QueryResponse<NetworkVersionInfo>>thenApply(response -> response);
    }

    @Override
    public String toString() {
        return "NetworkVersionInfoQuery[retryPolicy=" + retryPolicy()
                + ", attemptTimeout=" + attemptTimeout()
                + "]";
    }
}
