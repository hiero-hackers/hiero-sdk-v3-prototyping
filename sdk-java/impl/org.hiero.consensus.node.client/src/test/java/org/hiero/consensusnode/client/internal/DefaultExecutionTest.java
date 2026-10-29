package org.hiero.consensusnode.client.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import org.hiero.consensusnode.client.Attempt;
import org.hiero.consensusnode.client.AttemptOutcome;
import org.hiero.consensusnode.client.ExecutionSettings;
import org.hiero.consensusnode.client.GiveUp;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.NodeAnswer;
import org.hiero.consensusnode.client.RetryContext;
import org.hiero.consensusnode.client.RetryOnOtherNode;
import org.hiero.consensusnode.client.RetryOnSameNode;
import org.hiero.consensusnode.client.Submittable;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the default retry policy and node selector against the rules of "The default execution" in `client.md`.
@DisplayName("Default execution")
class DefaultExecutionTest {

    private static final ConsensusNode NODE_3 = node(3);
    private static final ConsensusNode NODE_4 = node(4);
    private static final ConsensusNode NODE_5 = node(5);

    private static ConsensusNode node(final long account) {
        return new ConsensusNode(new IpAddress(new byte[] {127, 0, 0, 1}), 50211,
                new AccountId(0L, 0L, "", account, null, null));
    }

    private static Attempt attempt(final int number, final ConsensusNode node, final AttemptOutcome outcome) {
        return new Attempt(number, node, new NodeAnswer(HapiTransactionStatus.BUSY), outcome, Duration.ZERO);
    }

    private static RetryContext after(final int number, final AttemptOutcome outcome) {
        final List<Attempt> attempts = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            attempts.add(attempt(i, NODE_3, outcome));
        }
        return new RetryContext(new Submittable<String>() {
            @Override
            public CompletionStage<String> submit(final HieroClient<?> client) {
                throw new UnsupportedOperationException();
            }
        }, attempts, Duration.ZERO);
    }

    @Test
    @DisplayName("the default settings have 10 attempts and an attempt timeout of 10 seconds")
    @Tag("testId:impl/default-execution/the-default-settings-have-10-attempts-of-10-seconds")
    void theDefaultSettingsHave10AttemptsOf10Seconds() {
        final ExecutionSettings settings = ExecutionSettings.createDefault();

        assertEquals(new DefaultRetryPolicy(10), settings.retryPolicy());
        assertInstanceOf(DefaultNodeSelector.class, settings.nodeSelector());
        assertEquals(Duration.ofSeconds(10), settings.attemptTimeout());
        assertEquals(List.of(), settings.listeners());
    }

    @Test
    @DisplayName("the default settings reject less than one attempt and an attempt timeout below 1 ms")
    @Tag("testId:impl/default-execution/the-default-settings-reject-invalid-values")
    void theDefaultSettingsRejectInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> ExecutionSettings.createDefault(0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> ExecutionSettings.createDefault(1, Duration.ZERO));
    }

    @Test
    @DisplayName("rule 7: the decision depends on the outcome of the last attempt")
    @Tag("testId:impl/default-execution/rule-7-the-decision-depends-on-the-outcome")
    void rule7TheDecisionDependsOnTheOutcome() {
        final DefaultRetryPolicy policy = new DefaultRetryPolicy(10);

        for (final AttemptOutcome outcome : List.of(AttemptOutcome.NODE_UNAVAILABLE, AttemptOutcome.NODE_TIMEOUT,
                AttemptOutcome.NODE_BUSY, AttemptOutcome.WRONG_NODE)) {
            assertEquals(new RetryOnOtherNode(Duration.ofMillis(250)), policy.decide(after(1, outcome)), outcome::name);
        }
        assertEquals(new RetryOnSameNode(Duration.ofMillis(250)), policy.decide(after(1, AttemptOutcome.PENDING)));
        assertInstanceOf(GiveUp.class, policy.decide(after(1, AttemptOutcome.REJECTED)));
    }

    @Test
    @DisplayName("rule 8: after the last attempt the policy gives up, whatever the outcome")
    @Tag("testId:impl/default-execution/rule-8-after-the-last-attempt-the-policy-gives-up")
    void rule8AfterTheLastAttemptThePolicyGivesUp() {
        final DefaultRetryPolicy policy = new DefaultRetryPolicy(3);

        assertEquals(new RetryOnSameNode(Duration.ofMillis(500)), policy.decide(after(2, AttemptOutcome.PENDING)));
        assertInstanceOf(GiveUp.class, policy.decide(after(3, AttemptOutcome.PENDING)));
        assertInstanceOf(GiveUp.class, new DefaultRetryPolicy(1).decide(after(1, AttemptOutcome.NODE_BUSY)));
    }

    @Test
    @DisplayName("rule 9: the waits double from 250 ms up to 8 s")
    @Tag("testId:impl/default-execution/rule-9-the-waits-double-from-250-ms-up-to-8-s")
    void rule9TheWaitsDoubleFrom250MsUpTo8S() {
        final List<Long> waits = new ArrayList<>();
        for (int attempt = 1; attempt <= 9; attempt++) {
            waits.add(DefaultRetryPolicy.delay(attempt).toMillis());
        }

        assertEquals(List.of(250L, 500L, 1_000L, 2_000L, 4_000L, 8_000L, 8_000L, 8_000L, 8_000L), waits);
        assertEquals(Duration.ofSeconds(8), DefaultRetryPolicy.delay(Integer.MAX_VALUE));
        // rule 10: with 10 attempts the waits add up to 39.75 s
        assertEquals(39_750L, waits.stream().mapToLong(Long::longValue).sum());
    }

    @Test
    @DisplayName("rule 12: a node is avoided for 8 s after a failure, doubled with every further one up to 1 hour")
    @Tag("testId:impl/default-execution/rule-12-a-failed-node-is-avoided-for-a-doubling-time")
    void rule12AFailedNodeIsAvoidedForADoublingTime() {
        final List<Long> avoidances = new ArrayList<>();
        for (int failures = 1; failures <= 11; failures++) {
            avoidances.add(DefaultNodeSelector.avoidance(failures).toSeconds());
        }

        assertEquals(List.of(8L, 16L, 32L, 64L, 128L, 256L, 512L, 1_024L, 2_048L, 3_600L, 3_600L), avoidances);
        assertEquals(Duration.ofHours(1), DefaultNodeSelector.avoidance(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("rules 12 and 13: a failed node is not chosen while it is avoided, and again afterwards")
    @Tag("testId:impl/default-execution/rules-12-and-13-a-failed-node-is-avoided")
    void rules12And13AFailedNodeIsAvoided() {
        final AtomicLong now = new AtomicLong();
        final DefaultNodeSelector selector = new DefaultNodeSelector(now::get);
        final List<ConsensusNode> candidates = List.of(NODE_3, NODE_4);

        selector.attemptCompleted(attempt(1, NODE_3, AttemptOutcome.NODE_UNAVAILABLE));
        for (int i = 0; i < 50; i++) {
            assertEquals(NODE_4, selector.select(candidates, List.of()));
        }
        now.set(Duration.ofSeconds(8).toNanos());

        assertEquals(Set.of(NODE_3, NODE_4), chosen(selector, candidates));
    }

    @Test
    @DisplayName("rule 12: an attempt with another outcome ends the avoidance, also BUSY")
    @Tag("testId:impl/default-execution/rule-12-another-outcome-ends-the-avoidance")
    void rule12AnotherOutcomeEndsTheAvoidance() {
        final DefaultNodeSelector selector = new DefaultNodeSelector(() -> 0L);
        final List<ConsensusNode> candidates = List.of(NODE_3, NODE_4);

        selector.attemptCompleted(attempt(1, NODE_3, AttemptOutcome.NODE_TIMEOUT));
        selector.attemptCompleted(attempt(2, NODE_3, AttemptOutcome.NODE_BUSY));

        assertEquals(Set.of(NODE_3, NODE_4), chosen(selector, candidates));
    }

    @Test
    @DisplayName("rule 13: when every candidate is avoided, the one whose avoidance ends first is chosen")
    @Tag("testId:impl/default-execution/rule-13-the-avoidance-that-ends-first-wins")
    void rule13TheAvoidanceThatEndsFirstWins() {
        final AtomicLong now = new AtomicLong();
        final DefaultNodeSelector selector = new DefaultNodeSelector(now::get);

        selector.attemptCompleted(attempt(1, NODE_3, AttemptOutcome.WRONG_NODE));
        selector.attemptCompleted(attempt(2, NODE_3, AttemptOutcome.WRONG_NODE)); // 16 s from 0
        now.set(Duration.ofSeconds(1).toNanos());
        selector.attemptCompleted(attempt(3, NODE_4, AttemptOutcome.NODE_UNAVAILABLE)); // 8 s from 1 s
        selector.attemptCompleted(attempt(4, NODE_5, AttemptOutcome.NODE_UNAVAILABLE)); // 8 s from 1 s

        assertEquals(Set.of(NODE_4, NODE_5), chosen(selector, List.of(NODE_3, NODE_4, NODE_5)));
        assertEquals(NODE_3, selector.select(List.of(NODE_3), List.of()));
    }

    @Test
    @DisplayName("rule 13: the choice among the nodes that are not avoided is random")
    @Tag("testId:impl/default-execution/rule-13-the-choice-is-random")
    void rule13TheChoiceIsRandom() {
        final DefaultNodeSelector selector = new DefaultNodeSelector(() -> 0L);

        assertEquals(Set.of(NODE_3, NODE_4, NODE_5), chosen(selector, List.of(NODE_3, NODE_4, NODE_5)));
        assertThrows(IllegalArgumentException.class, () -> selector.select(List.of(), List.of()));
    }

    /// The nodes the selector chooses in many selections; a random choice reaches every possible node.
    private static Set<ConsensusNode> chosen(final DefaultNodeSelector selector, final List<ConsensusNode> candidates) {
        final Set<ConsensusNode> chosen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            final ConsensusNode node = selector.select(candidates, List.of());
            assertTrue(candidates.contains(node));
            chosen.add(node);
        }
        return chosen;
    }
}
