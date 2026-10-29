package org.hiero.consensusnode.client.internal;

import java.time.Duration;
import org.hiero.consensusnode.client.Attempt;
import org.hiero.consensusnode.client.GiveUp;
import org.hiero.consensusnode.client.RetryContext;
import org.hiero.consensusnode.client.RetryDecision;
import org.hiero.consensusnode.client.RetryOnOtherNode;
import org.hiero.consensusnode.client.RetryOnSameNode;
import org.hiero.consensusnode.client.RetryPolicy;

/// The retry policy of `ExecutionSettings.createDefault(maxAttempts, attemptTimeout)`: rules 7 to 9 of the section
/// "The default execution" of `client.md`. Stateless, so one instance serves all requests.
///
/// @param maxAttempts the number of attempts after which it gives up, at least 1
public record DefaultRetryPolicy(int maxAttempts) implements RetryPolicy {

    /// The wait before the second attempt.
    static final Duration MIN_DELAY = Duration.ofMillis(250);

    /// The longest wait between two attempts.
    static final Duration MAX_DELAY = Duration.ofSeconds(8);

    /// Creates the policy.
    ///
    /// @throws IllegalArgumentException if `maxAttempts` is less than 1
    public DefaultRetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, but is " + maxAttempts);
        }
    }

    @Override
    public RetryDecision decide(final RetryContext context) {
        final Attempt last = context.attempts().getLast();
        if (last.number() >= maxAttempts) {
            return new GiveUp();
        }
        final Duration delay = delay(last.number());
        return switch (last.outcome()) {
            case NODE_UNAVAILABLE, NODE_TIMEOUT, NODE_BUSY, WRONG_NODE -> new RetryOnOtherNode(delay);
            case PENDING -> new RetryOnSameNode(delay);
            case THROTTLED, REJECTED, SUCCESS -> new GiveUp();
        };
    }

    /// Rule 9: the wait before attempt n + 1, min(250 ms × 2^(n − 1), 8 s).
    ///
    /// @param attempt the number n of the attempt that ended
    /// @return the wait
    static Duration delay(final int attempt) {
        // 250 ms × 2^5 = 8 s: from the sixth attempt on the wait is the maximum, and the shift cannot overflow
        return attempt > 6 ? MAX_DELAY : min(MIN_DELAY.multipliedBy(1L << (attempt - 1)), MAX_DELAY);
    }

    private static Duration min(final Duration a, final Duration b) {
        return a.compareTo(b) <= 0 ? a : b;
    }
}
