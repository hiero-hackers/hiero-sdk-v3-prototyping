package org.hiero.consensusnode.client;

import org.hiero.sdk.annotation.ThreadSafe;

/// Decides after every attempt that did not succeed whether and where the request is tried again. Implement it to
/// use your own strategy. A policy is shared by all requests that use it and must not keep state of a request.
@ThreadSafe
public interface RetryPolicy {

    /// The decision after the last attempt of the context, which did not succeed.
    RetryDecision decide(final RetryContext context);
}
