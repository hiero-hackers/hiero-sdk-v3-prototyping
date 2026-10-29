package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.consensusnode.queries.accounts.AccountBalanceQuery;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `RetryContext`, generated from the specs.
@DisplayName("RetryContext")
class RetryContextTest {

    @Test
    @DisplayName("creates a RetryContext and returns the values")
    @Tag("testId:consensusnode.client.RetryContext/create")
    void shouldCreateAndReturnTheValues() {
        final Submittable<?> request = requestValue();
        final List<Attempt> attempts = attemptsValue();
        final Duration elapsed = elapsedValue();
        final RetryContext subject = new RetryContext(request, attempts, elapsed);
        assertValue(request, subject.request());
        assertValue(attempts, subject.attempts());
        assertValue(elapsed, subject.elapsed());
    }

    @Test
    @DisplayName("rejects null for request")
    @Tag("testId:consensusnode.client.RetryContext/request/rejects-null")
    void shouldRejectNullRequest() {
        assertThrows(NullPointerException.class, () -> new RetryContext((Submittable<?>) null, attemptsValue(), elapsedValue()));
    }

    @Test
    @DisplayName("rejects null for attempts")
    @Tag("testId:consensusnode.client.RetryContext/attempts/rejects-null")
    void shouldRejectNullAttempts() {
        assertThrows(NullPointerException.class, () -> new RetryContext(requestValue(), (List<Attempt>) null, elapsedValue()));
    }

    @Test
    @DisplayName("the constructor copies attempts; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.client.RetryContext/attempts/copies-in")
    void shouldCopyAttemptsIn() {
        final List<Attempt> value = new ArrayList<>(attemptsValue());
        final List<Attempt> expected = List.copyOf(value);
        final RetryContext subject = new RetryContext(requestValue(), value, elapsedValue());
        value.clear();
        assertValue(expected, subject.attempts());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.attempts()).clear());
    }

    @Test
    @DisplayName("rejects null for elapsed")
    @Tag("testId:consensusnode.client.RetryContext/elapsed/rejects-null")
    void shouldRejectNullElapsed() {
        assertThrows(NullPointerException.class, () -> new RetryContext(requestValue(), attemptsValue(), (Duration) null));
    }

    private static Submittable<?> requestValue() {
        return new AccountBalanceQuery(new AccountId(0L, 0L, "", 1001L, null, null), null);
    }

    private static List<Attempt> attemptsValue() {
        return List.of(new Attempt(1, new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null)), new NodeAnswer(BasicTransactionStatus.OK), AttemptOutcome.SUCCESS, Duration.ofSeconds(1L)));
    }

    private static Duration elapsedValue() {
        return Duration.ofSeconds(1L);
    }

    private static RetryContext create() {
        return new RetryContext(requestValue(), attemptsValue(), elapsedValue());
    }
}
