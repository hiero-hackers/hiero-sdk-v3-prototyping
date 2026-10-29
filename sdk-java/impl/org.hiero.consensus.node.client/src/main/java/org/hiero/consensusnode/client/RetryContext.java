package org.hiero.consensusnode.client;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/// What a `RetryPolicy` knows when it decides.
///
/// @param request the request, e.g. to tell a transaction from a query
/// @param attempts the attempts of the request so far, the last one at the end; never empty
/// @param elapsed the time since the first attempt started
public record RetryContext(Submittable<?> request, List<Attempt> attempts, Duration elapsed) {

    /// Creates a new `RetryContext`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public RetryContext {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(attempts, "attempts must not be null");
        attempts = List.copyOf(attempts);
        Objects.requireNonNull(elapsed, "elapsed must not be null");
    }
}
