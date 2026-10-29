package org.hiero.consensusnode.client.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.hiero.consensusnode.client.Attempt;
import org.hiero.consensusnode.client.AttemptOutcome;
import org.hiero.consensusnode.client.AttemptsExhaustedException;
import org.hiero.consensusnode.client.ExecutionSettings;
import org.hiero.consensusnode.client.GiveUp;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.NodeAnswer;
import org.hiero.consensusnode.client.NodeSelector;
import org.hiero.consensusnode.client.RetryOnOtherNode;
import org.hiero.consensusnode.client.RetryOnSameNode;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.Submittable;
import org.hiero.consensusnode.client.TransportFailure;
import org.hiero.consensusnode.client.TransportFailureKind;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the steps every execution shares (rules 4 to 6, 13, 14 and 17 of "The default execution" in
/// `client.md`) and of the classification of answers ("The outcome of an attempt"). The policies used here do not wait.
@DisplayName("Execution")
class ExecutionTest {

    private static final ConsensusNode NODE_3 = node(3);
    private static final ConsensusNode NODE_4 = node(4);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static ConsensusNode node(final long account) {
        return new ConsensusNode(new IpAddress(new byte[] {127, 0, 0, 1}), 50211,
                new AccountId(0L, 0L, "", account, null, null));
    }

    /// A request that is only executed, never submitted.
    private static Submittable<String> request() {
        return new Submittable<>() {
            @Override
            public CompletionStage<String> submit(final HieroClient<?> client) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static Execution.Answer<String> success() {
        return new Execution.Answer<>(HapiTransactionStatus.OK, AttemptOutcome.SUCCESS, "value");
    }

    private static Execution.Answer<String> busy() {
        return Execution.Answer.precheck(HapiTransactionStatus.BUSY, false, () -> "value");
    }

    /// Sends the given answers one after the other and records the nodes and timeouts of the attempts.
    private static final class Script implements Execution.Send<String> {

        private final Deque<Object> answers;
        private final List<ConsensusNode> nodes = new ArrayList<>();
        private final List<Duration> timeouts = new ArrayList<>();

        Script(final Object... answers) {
            this.answers = new ArrayDeque<>(List.of(answers));
        }

        @Override
        @SuppressWarnings("unchecked")
        public Execution.Answer<String> send(final ConsensusNode node, final Duration timeout) {
            nodes.add(node);
            timeouts.add(timeout);
            final Object next = answers.removeFirst();
            if (next instanceof TransportException failure) {
                throw failure;
            }
            return (Execution.Answer<String>) next;
        }
    }

    /// Picks the first candidate and records what it is told.
    private static final class FirstSelector implements NodeSelector {

        private final List<List<ConsensusNode>> candidates = new ArrayList<>();
        private final List<String> events;

        FirstSelector(final List<String> events) {
            this.events = events;
        }

        @Override
        public ConsensusNode select(final List<ConsensusNode> nodes, final List<Attempt> attempts) {
            candidates.add(nodes);
            return nodes.getFirst();
        }

        @Override
        public void attemptCompleted(final Attempt attempt) {
            events.add("selector " + attempt.number());
        }
    }

    private static ExecutionSettings settings(final RetryPolicy policy, final NodeSelector selector,
                                              final List<String> events) {
        return new ExecutionSettings(policy, selector, TIMEOUT, List.of(
                (request, attempt) -> events.add("listener " + attempt.number())));
    }

    @Test
    @DisplayName("a successful first attempt returns its value after the selector and the listeners were told")
    @Tag("testId:impl/execution/a-successful-first-attempt-returns-its-value")
    void aSuccessfulFirstAttemptReturnsItsValue() {
        final List<String> events = new ArrayList<>();
        final Script script = new Script(success());

        final String value = Execution.run(settings(context -> new GiveUp(), new FirstSelector(events), events),
                request(), List.of(NODE_3, NODE_4), script);

        assertEquals("value", value);
        assertEquals(List.of("selector 1", "listener 1"), events);
        assertEquals(List.of(NODE_3), script.nodes);
        assertEquals(List.of(TIMEOUT), script.timeouts);
    }

    @Test
    @DisplayName("an error of a listener neither changes nor ends the request")
    @Tag("testId:impl/execution/an-error-of-a-listener-neither-changes-nor-ends-the-request")
    void anErrorOfAListenerNeitherChangesNorEndsTheRequest() {
        final ExecutionSettings settings = new ExecutionSettings(context -> new RetryOnSameNode(Duration.ZERO),
                new FirstSelector(new ArrayList<>()), TIMEOUT, List.of((request, attempt) -> {
                    throw new IllegalStateException("listener failed");
                }));

        assertEquals("value", Execution.run(settings, request(), List.of(NODE_3),
                new Script(Execution.Answer.precheck(HapiTransactionStatus.UNKNOWN, true, () -> "value"), success())));
    }

    @Test
    @DisplayName("a retry on another node chooses among the candidates without the node of the last attempt")
    @Tag("testId:impl/execution/a-retry-on-another-node-leaves-out-the-node-of-the-last-attempt")
    void aRetryOnAnotherNodeLeavesOutTheNodeOfTheLastAttempt() {
        final FirstSelector selector = new FirstSelector(new ArrayList<>());
        final Script script = new Script(busy(), success());

        Execution.run(settings(context -> new RetryOnOtherNode(Duration.ZERO), selector, new ArrayList<>()),
                request(), List.of(NODE_3, NODE_4), script);

        assertEquals(List.of(List.of(NODE_3, NODE_4), List.of(NODE_4)), selector.candidates);
        assertEquals(List.of(NODE_3, NODE_4), script.nodes);
    }

    @Test
    @DisplayName("a retry on another node uses the same node when it is the only candidate")
    @Tag("testId:impl/execution/a-retry-on-another-node-uses-the-only-candidate-again")
    void aRetryOnAnotherNodeUsesTheOnlyCandidateAgain() {
        final Script script = new Script(busy(), success());

        Execution.run(settings(context -> new RetryOnOtherNode(Duration.ZERO), new FirstSelector(new ArrayList<>()),
                new ArrayList<>()), request(), List.of(NODE_3), script);

        assertEquals(List.of(NODE_3, NODE_3), script.nodes);
    }

    @Test
    @DisplayName("a retry on the same node asks the node selector for no node")
    @Tag("testId:impl/execution/a-retry-on-the-same-node-asks-the-selector-for-no-node")
    void aRetryOnTheSameNodeAsksTheSelectorForNoNode() {
        final FirstSelector selector = new FirstSelector(new ArrayList<>());
        final Script script = new Script(busy(), busy(), success());

        Execution.run(settings(context -> new RetryOnSameNode(Duration.ZERO), selector, new ArrayList<>()),
                request(), List.of(NODE_3, NODE_4), script);

        assertEquals(1, selector.candidates.size());
        assertEquals(List.of(NODE_3, NODE_3, NODE_3), script.nodes);
    }

    @Test
    @DisplayName("the retry policy sees every attempt of the request")
    @Tag("testId:impl/execution/the-retry-policy-sees-every-attempt-of-the-request")
    void theRetryPolicySeesEveryAttemptOfTheRequest() {
        final List<List<Attempt>> seen = new ArrayList<>();
        final RetryPolicy policy = context -> {
            seen.add(context.attempts());
            return new RetryOnSameNode(Duration.ZERO);
        };

        Execution.run(settings(policy, new FirstSelector(new ArrayList<>()), new ArrayList<>()), request(),
                List.of(NODE_3), new Script(busy(), new TransportException(TransportFailureKind.TIMEOUT, "late"),
                        success()));

        assertEquals(2, seen.size());
        final Attempt first = seen.get(1).getFirst();
        final Attempt second = seen.get(1).get(1);
        assertEquals(1, first.number());
        assertEquals(AttemptOutcome.NODE_BUSY, first.outcome());
        assertEquals(new NodeAnswer(HapiTransactionStatus.BUSY), first.result());
        assertEquals(2, second.number());
        assertEquals(AttemptOutcome.NODE_TIMEOUT, second.outcome());
        assertEquals(new TransportFailure(TransportFailureKind.TIMEOUT, "late"), second.result());
    }

    @Test
    @DisplayName("the retry policy and the attempt timeout of the request replace those of the settings")
    @Tag("testId:impl/execution/the-settings-of-the-request-replace-those-of-the-client")
    void theSettingsOfTheRequestReplaceThoseOfTheClient() {
        final Submittable<String> request = request();
        request.setRetryPolicy(context -> new RetryOnSameNode(Duration.ZERO));
        request.setAttemptTimeout(Duration.ofSeconds(3));
        final Script script = new Script(busy(), success());

        Execution.run(settings(context -> new GiveUp(), new FirstSelector(new ArrayList<>()), new ArrayList<>()),
                request, List.of(NODE_3), script);

        assertEquals(List.of(Duration.ofSeconds(3), Duration.ofSeconds(3)), script.timeouts);
    }

    @Test
    @DisplayName("a request the retry policy gives up fails with the status of a rejection as its cause")
    @Tag("testId:impl/execution/a-rejected-request-fails-with-the-status-as-cause")
    void aRejectedRequestFailsWithTheStatusAsCause() {
        final AttemptsExhaustedException error = assertThrows(AttemptsExhaustedException.class, () -> Execution.run(
                settings(context -> new GiveUp(), new FirstSelector(new ArrayList<>()), new ArrayList<>()),
                request(), List.of(NODE_3), new Script(Execution.Answer.precheck(
                        HapiTransactionStatus.INSUFFICIENT_PAYER_BALANCE, false, () -> "value"))));

        final PrecheckException cause = assertInstanceOf(PrecheckException.class, error.getCause());
        assertEquals(HapiTransactionStatus.INSUFFICIENT_PAYER_BALANCE, cause.status());
    }

    @Test
    @DisplayName("a request that gives up after a transport failure has no status")
    @Tag("testId:impl/execution/a-request-that-gives-up-after-a-transport-failure-has-no-status")
    void aRequestThatGivesUpAfterATransportFailureHasNoStatus() {
        final AttemptsExhaustedException error = assertThrows(AttemptsExhaustedException.class, () -> Execution.run(
                settings(context -> new GiveUp(), new FirstSelector(new ArrayList<>()), new ArrayList<>()),
                request(), List.of(NODE_3), new Script(new TransportException(TransportFailureKind.CONNECTION,
                        "refused"))));

        assertNull(error.getCause());
    }

    @Test
    @DisplayName("a node selector that chooses no candidate is an error")
    @Tag("testId:impl/execution/a-node-selector-that-chooses-no-candidate-is-an-error")
    void aNodeSelectorThatChoosesNoCandidateIsAnError() {
        final NodeSelector selector = new NodeSelector() {
            @Override
            public ConsensusNode select(final List<ConsensusNode> candidates, final List<Attempt> attempts) {
                return NODE_4;
            }

            @Override
            public void attemptCompleted(final Attempt attempt) {
            }
        };

        assertThrows(IllegalStateException.class, () -> Execution.run(settings(context -> new GiveUp(), selector,
                new ArrayList<>()), request(), List.of(NODE_3), new Script(success())));
    }

    @Test
    @DisplayName("the precheck statuses have the outcomes of the specification")
    @Tag("testId:impl/execution/the-precheck-statuses-have-the-outcomes-of-the-specification")
    void thePrecheckStatusesHaveTheOutcomesOfTheSpecification() {
        assertEquals(AttemptOutcome.SUCCESS, Execution.outcome(HapiTransactionStatus.OK, false));
        assertEquals(AttemptOutcome.NODE_BUSY, Execution.outcome(HapiTransactionStatus.BUSY, false));
        assertEquals(AttemptOutcome.NODE_BUSY,
                Execution.outcome(HapiTransactionStatus.PLATFORM_TRANSACTION_NOT_CREATED, false));
        assertEquals(AttemptOutcome.NODE_BUSY, Execution.outcome(HapiTransactionStatus.PLATFORM_NOT_ACTIVE, false));
        assertEquals(AttemptOutcome.WRONG_NODE, Execution.outcome(HapiTransactionStatus.INVALID_NODE_ACCOUNT, false));
        assertEquals(AttemptOutcome.PENDING, Execution.outcome(HapiTransactionStatus.UNKNOWN, true));
        assertEquals(AttemptOutcome.PENDING, Execution.outcome(HapiTransactionStatus.RECEIPT_NOT_FOUND, true));
        assertEquals(AttemptOutcome.PENDING, Execution.outcome(HapiTransactionStatus.RECORD_NOT_FOUND, true));
        assertEquals(AttemptOutcome.REJECTED, Execution.outcome(HapiTransactionStatus.UNKNOWN, false));
        assertEquals(AttemptOutcome.REJECTED, Execution.outcome(HapiTransactionStatus.DUPLICATE_TRANSACTION, false));
        assertEquals(AttemptOutcome.REJECTED, Execution.outcome(HapiTransactionStatus.INVALID_SIGNATURE, false));
    }

    @Test
    @DisplayName("a receipt is pending while its status is UNKNOWN or OK")
    @Tag("testId:impl/execution/a-receipt-is-pending-while-its-status-is-unknown-or-ok")
    void aReceiptIsPendingWhileItsStatusIsUnknownOrOk() {
        assertEquals(AttemptOutcome.PENDING, Execution.receiptOutcome(HapiTransactionStatus.UNKNOWN));
        assertEquals(AttemptOutcome.PENDING, Execution.receiptOutcome(HapiTransactionStatus.OK));
        assertEquals(AttemptOutcome.SUCCESS, Execution.receiptOutcome(HapiTransactionStatus.SUCCESS));
        assertEquals(AttemptOutcome.SUCCESS, Execution.receiptOutcome(HapiTransactionStatus.INVALID_SIGNATURE));
    }

    @Test
    @DisplayName("the transport failures have the outcomes and the gRPC codes the kinds of the specification")
    @Tag("testId:impl/execution/the-transport-failures-have-the-outcomes-of-the-specification")
    void theTransportFailuresHaveTheOutcomesOfTheSpecification() {
        assertEquals(AttemptOutcome.NODE_UNAVAILABLE, Execution.outcome(TransportFailureKind.CONNECTION));
        assertEquals(AttemptOutcome.NODE_UNAVAILABLE, Execution.outcome(TransportFailureKind.OVERLOADED));
        assertEquals(AttemptOutcome.NODE_UNAVAILABLE, Execution.outcome(TransportFailureKind.INTERRUPTED));
        assertEquals(AttemptOutcome.NODE_TIMEOUT, Execution.outcome(TransportFailureKind.TIMEOUT));
        assertEquals(AttemptOutcome.REJECTED, Execution.outcome(TransportFailureKind.PROTOCOL));

        assertEquals(TransportFailureKind.CONNECTION, Grpc.kind(Status.UNAVAILABLE));
        assertEquals(TransportFailureKind.TIMEOUT, Grpc.kind(Status.DEADLINE_EXCEEDED));
        assertEquals(TransportFailureKind.OVERLOADED, Grpc.kind(Status.RESOURCE_EXHAUSTED));
        assertEquals(TransportFailureKind.INTERRUPTED, Grpc.kind(Status.CANCELLED));
        assertEquals(TransportFailureKind.INTERRUPTED,
                Grpc.kind(Status.INTERNAL.withDescription("Received RST_STREAM with code 2")));
        assertEquals(TransportFailureKind.PROTOCOL, Grpc.kind(Status.INTERNAL.withDescription("parse error")));
        assertEquals(TransportFailureKind.PROTOCOL, Grpc.kind(Status.UNIMPLEMENTED));
    }
}
