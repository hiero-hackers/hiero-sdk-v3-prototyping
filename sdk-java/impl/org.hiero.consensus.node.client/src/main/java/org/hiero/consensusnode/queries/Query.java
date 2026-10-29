package org.hiero.consensusnode.queries;

import java.time.Duration;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.Submittable;
import org.jspecify.annotations.Nullable;

/// A read-only request to a consensus node. Free queries are `FreeQuery` subtypes, queries that require payment are
/// `PaidQuery` subtypes; use `Query` where both kinds are accepted.
///
/// Use the inherited retry settings to tune how the request is sent and `submit(client)` to send it. The result
/// is returned wrapped in the response envelope `$$Response`: a `QueryResponse` for free queries, a
/// `PaidQueryResponse` for paid ones.
public abstract class Query<Result, ResponseT extends QueryResponse<Result>> extends Submittable<ResponseT> {

    /// Creates a new `Query`.
    protected Query() {
        super();
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    @Override
    public Query<Result, ResponseT> setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        super.setRetryPolicy(retryPolicy);
        return this;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    @Override
    public Query<Result, ResponseT> setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        super.setAttemptTimeout(attemptTimeout);
        return this;
    }
}
