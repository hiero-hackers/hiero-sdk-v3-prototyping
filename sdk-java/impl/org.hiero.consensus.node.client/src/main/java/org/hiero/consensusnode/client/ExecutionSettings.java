package org.hiero.consensusnode.client;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.hiero.consensusnode.client.internal.DefaultNodeSelector;
import org.hiero.consensusnode.client.internal.DefaultRetryPolicy;

/// How a client executes requests: which node an attempt goes to, whether and when a failed attempt is repeated, how
/// long a single attempt may take, and who is told about each attempt.
/// A language that has its own means to run asynchronous work (an executor, a scheduler, a runtime handle) adds an
/// attribute for it here, with a default, so that callers can choose where the work of the SDK runs.
///
/// @param retryPolicy decides whether and where a request is tried again
/// @param nodeSelector picks the node of an attempt
/// @param attemptTimeout the time one attempt may take
/// @param listeners told about every attempt
public record ExecutionSettings(
        RetryPolicy retryPolicy,
        NodeSelector nodeSelector,
        Duration attemptTimeout,
        List<AttemptListener> listeners) {

    /// Creates a new `ExecutionSettings`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ExecutionSettings {
        Objects.requireNonNull(retryPolicy, "retryPolicy must not be null");
        Objects.requireNonNull(nodeSelector, "nodeSelector must not be null");
        Objects.requireNonNull(attemptTimeout, "attemptTimeout must not be null");
        Objects.requireNonNull(listeners, "listeners must not be null");
        listeners = List.copyOf(listeners);
    }

    /// Creates a new `ExecutionSettings` with the default value of `listeners`.
    public ExecutionSettings(
            final RetryPolicy retryPolicy,
            final NodeSelector nodeSelector,
            final Duration attemptTimeout) {
        this(retryPolicy, nodeSelector, attemptTimeout, List.of());
    }

    /// The execution settings of the SDK: `createDefault(maxAttempts, attemptTimeout)` with 10 attempts and an attempt
    /// timeout of 10 seconds. A request takes at most 10 attempts of 10 seconds each plus the waits between them
    /// (39.75 seconds), about 2 minutes and 20 seconds.
    public static ExecutionSettings createDefault() {
        return createDefault(10, Duration.ofSeconds(10));
    }

    /// The execution settings of the SDK with the given number of attempts and attempt timeout, and no listeners. They
    /// execute a request as the section "The default execution" of this API describes: on another node after a node
    /// failure, on the same node while a receipt is pending, giving up after `THROTTLED`, a rejection or the last
    /// attempt; waiting 250 ms before the second attempt, doubled up to 8 s; avoiding a failed node for 8 s, doubled
    /// up to 1 hour. A request takes at most `maxAttempts` times `attemptTimeout` plus the waits between the attempts.
    public static ExecutionSettings createDefault(final int maxAttempts, final Duration attemptTimeout) {
        Objects.requireNonNull(attemptTimeout, "attemptTimeout must not be null");
        if (attemptTimeout.compareTo(Duration.ofMillis(1)) < 0) {
            throw new IllegalArgumentException("attemptTimeout must be at least 1 ms, but is " + attemptTimeout);
        }
        return new ExecutionSettings(new DefaultRetryPolicy(maxAttempts), new DefaultNodeSelector(), attemptTimeout);
    }
}
