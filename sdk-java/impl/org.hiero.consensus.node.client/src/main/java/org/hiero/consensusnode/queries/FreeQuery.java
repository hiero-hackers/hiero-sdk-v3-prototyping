package org.hiero.consensusnode.queries;

import java.time.Duration;
import org.hiero.consensusnode.client.RetryPolicy;
import org.jspecify.annotations.Nullable;

/// A query that the network answers without charging the caller, for example an account balance or a transaction
/// receipt. `submit(client)` returns the result wrapped in a `QueryResponse`.
public abstract class FreeQuery<Result> extends Query<Result, QueryResponse<Result>> {

    /// Creates a new `FreeQuery`.
    protected FreeQuery() {
        super();
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public FreeQuery<Result> setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public FreeQuery<Result> setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }
}
