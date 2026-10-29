package org.hiero.consensusnode.client.internal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.hiero.consensusnode.client.Attempt;
import org.hiero.consensusnode.client.AttemptListener;
import org.hiero.consensusnode.client.AttemptOutcome;
import org.hiero.consensusnode.client.AttemptResult;
import org.hiero.consensusnode.client.AttemptsExhaustedException;
import org.hiero.consensusnode.client.ExecutionSettings;
import org.hiero.consensusnode.client.GiveUp;
import org.hiero.consensusnode.client.NodeAnswer;
import org.hiero.consensusnode.client.NodeSelector;
import org.hiero.consensusnode.client.RetryContext;
import org.hiero.consensusnode.client.RetryDecision;
import org.hiero.consensusnode.client.RetryOnOtherNode;
import org.hiero.consensusnode.client.RetryOnSameNode;
import org.hiero.consensusnode.client.RetryPolicy;
import org.hiero.consensusnode.client.Submittable;
import org.hiero.consensusnode.client.TransportFailure;
import org.hiero.consensusnode.client.TransportFailureKind;
import org.hiero.ledger.ConsensusNode;
import org.jspecify.annotations.Nullable;

/// Executes a request in attempts, as the section "The default execution" of `client.md` specifies the steps every
/// execution shares (rules 4 to 6, 13, 14 and 17): send to a node, classify the answer, tell the node selector and the
/// listeners, ask the retry policy, wait, and pick the node of the next attempt. What a decision is and how long a node
/// is avoided is up to the `RetryPolicy` and the `NodeSelector` of the settings.
///
/// Runs on the calling thread and blocks while it waits; the callers run it on the executor of the client.
public final class Execution {

    private Execution() {
    }

    /// One attempt: sends the request to a node and returns its answer.
    ///
    /// @param <T> the type of the value of a successful request
    @FunctionalInterface
    public interface Send<T> {

        /// Sends the request to the node.
        ///
        /// @param node    the node
        /// @param timeout the time the attempt may take
        /// @return the answer of the node
        /// @throws TransportException if no answer arrived
        Answer<T> send(ConsensusNode node, Duration timeout);
    }

    /// The answer of a node, classified.
    ///
    /// @param status  the status the node reported
    /// @param outcome the outcome of the attempt
    /// @param value   the value of the request, present exactly when the outcome is `SUCCESS`
    /// @param <T> the type of the value
    public record Answer<T>(HapiTransactionStatus status, AttemptOutcome outcome, @Nullable T value) {

        /// Creates an answer.
        ///
        /// @throws IllegalArgumentException if the value is present without success or absent with it
        public Answer {
            Objects.requireNonNull(status, "status must not be null");
            Objects.requireNonNull(outcome, "outcome must not be null");
            if ((outcome == AttemptOutcome.SUCCESS) != (value != null)) {
                throw new IllegalArgumentException("A value belongs to a successful answer and only to it");
            }
        }

        /// The answer to the precheck of a request.
        ///
        /// @param precheck        the precheck status
        /// @param receiptOrRecord whether the request asks for a receipt or a record, which may not be available yet
        /// @param value           supplies the value when the precheck succeeded
        /// @param <T> the type of the value
        /// @return the answer
        public static <T> Answer<T> precheck(final HapiTransactionStatus precheck, final boolean receiptOrRecord,
                                             final Supplier<T> value) {
            final AttemptOutcome outcome = Execution.outcome(precheck, receiptOrRecord);
            return new Answer<>(precheck, outcome, outcome == AttemptOutcome.SUCCESS ? value.get() : null);
        }
    }

    /// Executes a request.
    ///
    /// @param settings   the execution settings of the client
    /// @param request    the request; its retry policy and attempt timeout replace those of the settings
    /// @param candidates the nodes the attempts may go to, not empty
    /// @param send       sends one attempt
    /// @param <T> the type of the value
    /// @return the value of the successful attempt
    /// @throws AttemptsExhaustedException if the retry policy gives up
    public static <T> T run(final ExecutionSettings settings, final Submittable<?> request,
                            final List<ConsensusNode> candidates, final Send<T> send) {
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("A request needs at least one node to go to");
        }
        final RetryPolicy policy = request.retryPolicy() != null ? request.retryPolicy() : settings.retryPolicy();
        final Duration timeout = request.attemptTimeout() != null ? request.attemptTimeout()
                : settings.attemptTimeout();
        final NodeSelector selector = settings.nodeSelector();
        final List<Attempt> attempts = new ArrayList<>();
        final long start = System.nanoTime();
        ConsensusNode node = select(selector, candidates, attempts);
        for (int number = 1; ; number++) {
            final Sent<T> sent = attempt(number, node, send, timeout);
            final Attempt attempt = sent.attempt();
            attempts.add(attempt);
            selector.attemptCompleted(attempt);
            for (final AttemptListener listener : settings.listeners()) {
                try {
                    listener.attemptCompleted(request, attempt);
                } catch (final RuntimeException ignored) {
                    // rule 6: an error of a listener neither changes nor ends the request
                }
            }
            if (sent.value() != null) {
                return sent.value();
            }
            final RetryDecision decision = policy.decide(new RetryContext(request, List.copyOf(attempts),
                    Duration.ofNanos(System.nanoTime() - start)));
            switch (decision) {
                case GiveUp giveUp -> throw exhausted(attempt);
                case RetryOnSameNode same -> ClientRuntime.sleep(same.delay());
                case RetryOnOtherNode other -> {
                    ClientRuntime.sleep(other.delay());
                    final ConsensusNode last = node;
                    final List<ConsensusNode> others = candidates.stream().filter(c -> !c.equals(last)).toList();
                    node = select(selector, others.isEmpty() ? candidates : others, attempts);
                }
            }
        }
    }

    /// An attempt and, when it succeeded, the value of the request.
    private record Sent<T>(Attempt attempt, @Nullable T value) {
    }

    private static <T> Sent<T> attempt(final int number, final ConsensusNode node, final Send<T> send,
                                       final Duration timeout) {
        final long begin = System.nanoTime();
        AttemptResult result;
        AttemptOutcome outcome;
        T value = null;
        try {
            final Answer<T> answer = send.send(node, timeout);
            result = new NodeAnswer(answer.status());
            outcome = answer.outcome();
            value = answer.value();
        } catch (final TransportException failure) {
            result = new TransportFailure(failure.kind(), String.valueOf(failure.getMessage()));
            outcome = outcome(failure.kind());
        }
        return new Sent<>(new Attempt(number, node, result, outcome, Duration.ofNanos(System.nanoTime() - begin)),
                value);
    }

    private static ConsensusNode select(final NodeSelector selector, final List<ConsensusNode> candidates,
                                        final List<Attempt> attempts) {
        final ConsensusNode node = selector.select(candidates, List.copyOf(attempts));
        if (!candidates.contains(node)) {
            throw new IllegalStateException("The node selector chose " + node.account()
                    + ", which is no candidate of the request");
        }
        return node;
    }

    /// Rule 17: the error of a request the retry policy gave up. When the node rejected the request in its precheck,
    /// the cause is a [PrecheckException] with the status.
    private static AttemptsExhaustedException exhausted(final Attempt last) {
        final String result = switch (last.result()) {
            case NodeAnswer answer -> "status " + ((HapiTransactionStatus) answer.status()).name();
            case TransportFailure failure -> failure.kind() + " (" + failure.message() + ")";
        };
        final @Nullable Throwable cause = last.result() instanceof NodeAnswer answer
                && last.outcome() != AttemptOutcome.PENDING
                ? new PrecheckException((HapiTransactionStatus) answer.status()) : null;
        return new AttemptsExhaustedException("Gave up after " + last.number() + " attempt(s); the last one, to node "
                + last.node().account() + ", ended " + last.outcome() + " with " + result, cause);
    }

    /// The outcome of a precheck status, as the section "The outcome of an attempt" of `client.md` specifies it.
    ///
    /// @param precheck        the precheck status
    /// @param receiptOrRecord whether the request asks for a receipt or a record
    /// @return the outcome
    public static AttemptOutcome outcome(final HapiTransactionStatus precheck, final boolean receiptOrRecord) {
        return switch (precheck) {
            case OK -> AttemptOutcome.SUCCESS;
            case BUSY, PLATFORM_TRANSACTION_NOT_CREATED, PLATFORM_NOT_ACTIVE -> AttemptOutcome.NODE_BUSY;
            case INVALID_NODE_ACCOUNT -> AttemptOutcome.WRONG_NODE;
            case UNKNOWN, RECEIPT_NOT_FOUND, RECORD_NOT_FOUND ->
                    receiptOrRecord ? AttemptOutcome.PENDING : AttemptOutcome.REJECTED;
            default -> AttemptOutcome.REJECTED;
        };
    }

    /// The outcome of the status of a receipt the node returned: pending while it is `UNKNOWN` or `OK`.
    ///
    /// @param status the status of the receipt
    /// @return the outcome
    public static AttemptOutcome receiptOutcome(final HapiTransactionStatus status) {
        return status == HapiTransactionStatus.UNKNOWN || status == HapiTransactionStatus.OK
                ? AttemptOutcome.PENDING : AttemptOutcome.SUCCESS;
    }

    /// The outcome of a transport failure.
    ///
    /// @param kind the kind of the failure
    /// @return the outcome
    public static AttemptOutcome outcome(final TransportFailureKind kind) {
        return switch (kind) {
            case CONNECTION, OVERLOADED, INTERRUPTED -> AttemptOutcome.NODE_UNAVAILABLE;
            case TIMEOUT -> AttemptOutcome.NODE_TIMEOUT;
            case PROTOCOL -> AttemptOutcome.REJECTED;
        };
    }
}
