package org.hiero.consensusnode.client.internal;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;
import org.hiero.consensusnode.client.Attempt;
import org.hiero.consensusnode.client.NodeSelector;
import org.hiero.ledger.ConsensusNode;

/// The node selector of `ExecutionSettings.createDefault(maxAttempts, attemptTimeout)`: rules 11 to 13 of the
/// section "The default execution" of `client.md`. Keeps, per node, the failures in a row and until when the node is
/// avoided; the state is shared by all requests of the clients that use the instance.
public final class DefaultNodeSelector implements NodeSelector {

    /// How long a node is avoided after its first failure.
    static final Duration MIN_AVOIDANCE = Duration.ofSeconds(8);

    /// The longest time a node is avoided.
    static final Duration MAX_AVOIDANCE = Duration.ofHours(1);

    /// The failures of a node in a row and the time (of the clock) until which it is avoided.
    private record Health(int failures, long avoidedUntil) {
    }

    private final Map<ConsensusNode, Health> health = new ConcurrentHashMap<>();

    private final LongSupplier clock;

    /// Creates a selector on the clock of the system.
    public DefaultNodeSelector() {
        this(System::nanoTime);
    }

    /// Creates a selector on a clock, for tests.
    ///
    /// @param clock the current time in nanoseconds, as `System.nanoTime()`
    DefaultNodeSelector(final LongSupplier clock) {
        this.clock = clock;
    }

    @Override
    public ConsensusNode select(final List<ConsensusNode> candidates, final List<Attempt> attempts) {
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("There is no candidate to select");
        }
        final long now = clock.getAsLong();
        final List<ConsensusNode> available = candidates.stream().filter(node -> avoidedUntil(node) <= now).toList();
        if (!available.isEmpty()) {
            return random(available);
        }
        // rule 13: every candidate is avoided - the one whose avoidance ends first, a random one of a tie
        final long first = candidates.stream().mapToLong(this::avoidedUntil).min().orElseThrow();
        return random(candidates.stream().filter(node -> avoidedUntil(node) == first).toList());
    }

    @Override
    public void attemptCompleted(final Attempt attempt) {
        switch (attempt.outcome()) {
            case NODE_UNAVAILABLE, NODE_TIMEOUT, WRONG_NODE -> {
                final long end = clock.getAsLong();
                health.compute(attempt.node(), (node, before) -> {
                    final int failures = before == null ? 1 : before.failures() + 1;
                    return new Health(failures, end + avoidance(failures).toNanos());
                });
            }
            default -> health.remove(attempt.node());
        }
    }

    /// Rule 12: how long a node is avoided after its k-th failure in a row, min(8 s × 2^(k − 1), 1 h).
    ///
    /// @param failures the failures in a row, at least 1
    /// @return the time
    static Duration avoidance(final int failures) {
        // 8 s × 2^9 = 4096 s > 1 h: from the tenth failure on it is the maximum, and the shift cannot overflow
        if (failures > 9) {
            return MAX_AVOIDANCE;
        }
        final Duration avoidance = MIN_AVOIDANCE.multipliedBy(1L << (failures - 1));
        return avoidance.compareTo(MAX_AVOIDANCE) <= 0 ? avoidance : MAX_AVOIDANCE;
    }

    private long avoidedUntil(final ConsensusNode node) {
        final Health state = health.get(node);
        return state == null ? Long.MIN_VALUE : state.avoidedUntil();
    }

    private static ConsensusNode random(final List<ConsensusNode> nodes) {
        return nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
    }
}
