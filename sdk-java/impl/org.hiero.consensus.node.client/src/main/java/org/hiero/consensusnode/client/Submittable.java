package org.hiero.consensusnode.client;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import org.jspecify.annotations.Nullable;

/// Common base for anything that is sent to the consensus node network and produces a typed result — both
/// queries (`Query`) and packed transactions (`PackedTransaction`). It is sent in attempts as the `ExecutionSettings`
/// of the client define; the retry policy and the attempt timeout can be overridden for this request.
public abstract class Submittable<Result> {

    private @Nullable RetryPolicy retryPolicy;
    private @Nullable Duration attemptTimeout;

    /// Creates a new `Submittable`.
    protected Submittable() {
    }

    /// the retry policy for this request; if absent, the one of the client is used
    public @Nullable RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    /// Sets the `retryPolicy`.
    ///
    /// @param retryPolicy the new value
    /// @return this object
    public Submittable<Result> setRetryPolicy(final @Nullable RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /// the time one attempt may take; if absent, the one of the client is used
    public @Nullable Duration attemptTimeout() {
        return attemptTimeout;
    }

    /// Sets the `attemptTimeout`.
    ///
    /// @param attemptTimeout the new value
    /// @return this object
    public Submittable<Result> setAttemptTimeout(final @Nullable Duration attemptTimeout) {
        this.attemptTimeout = attemptTimeout;
        return this;
    }

    /// Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    /// and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    /// handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    ///
    /// The returned stage completes exceptionally with `AttemptsExhaustedException` if the operation fails.
    public abstract CompletionStage<Result> submit(final HieroClient<?> client);
}
