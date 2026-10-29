package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.hiero.consensusnode.status.BasicTransactionStatus;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Attempt`, generated from the specs.
@DisplayName("Attempt")
class AttemptTest {

    @Test
    @DisplayName("creates an Attempt and returns the values")
    @Tag("testId:consensusnode.client.Attempt/create")
    void shouldCreateAndReturnTheValues() {
        final int number = numberValue();
        final ConsensusNode node = nodeValue();
        final AttemptResult result = resultValue();
        final AttemptOutcome outcome = outcomeValue();
        final Duration duration = durationValue();
        final Attempt subject = new Attempt(number, node, result, outcome, duration);
        assertValue(number, subject.number());
        assertValue(node, subject.node());
        assertValue(result, subject.result());
        assertValue(outcome, subject.outcome());
        assertValue(duration, subject.duration());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.client.Attempt/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final int number = numberValue();
        final ConsensusNode node = nodeValue();
        final AttemptResult result = resultValue();
        final AttemptOutcome outcome = outcomeValue();
        final Duration duration = durationValue();
        final Attempt subject = Attempt.builder()
                .number(number)
                .node(node)
                .result(result)
                .outcome(outcome)
                .duration(duration)
                .build();
        assertValue(number, subject.number());
        assertValue(node, subject.node());
        assertValue(result, subject.result());
        assertValue(outcome, subject.outcome());
        assertValue(duration, subject.duration());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.client.Attempt/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Attempt subject = Attempt.builder()
                .number(numberValue())
                .node(nodeValue())
                .result(resultValue())
                .outcome(outcomeValue())
                .duration(durationValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.client.Attempt/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Attempt.builder().build());
    }

    @Test
    @DisplayName("number: accepts the minimum (1)")
    @Tag("testId:consensusnode.client.Attempt/number/accepts-at-minimum")
    void shouldAcceptNumberAtMinimum() {
        final int value = 1;
        final Attempt subject = new Attempt(value, nodeValue(), resultValue(), outcomeValue(), durationValue());
        assertValue(value, subject.number());
    }

    @Test
    @DisplayName("number: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.client.Attempt/number/accepts-at-maximum")
    void shouldAcceptNumberAtMaximum() {
        final int value = 2147483647;
        final Attempt subject = new Attempt(value, nodeValue(), resultValue(), outcomeValue(), durationValue());
        assertValue(value, subject.number());
    }

    @Test
    @DisplayName("number: rejects a value below the minimum (0)")
    @Tag("testId:consensusnode.client.Attempt/number/rejects-below-minimum")
    void shouldRejectNumberBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new Attempt(0, nodeValue(), resultValue(), outcomeValue(), durationValue()));
    }

    @Test
    @DisplayName("rejects null for node")
    @Tag("testId:consensusnode.client.Attempt/node/rejects-null")
    void shouldRejectNullNode() {
        assertThrows(NullPointerException.class, () -> new Attempt(numberValue(), (ConsensusNode) null, resultValue(), outcomeValue(), durationValue()));
    }

    @Test
    @DisplayName("rejects null for result")
    @Tag("testId:consensusnode.client.Attempt/result/rejects-null")
    void shouldRejectNullResult() {
        assertThrows(NullPointerException.class, () -> new Attempt(numberValue(), nodeValue(), (AttemptResult) null, outcomeValue(), durationValue()));
    }

    @Test
    @DisplayName("rejects null for outcome")
    @Tag("testId:consensusnode.client.Attempt/outcome/rejects-null")
    void shouldRejectNullOutcome() {
        assertThrows(NullPointerException.class, () -> new Attempt(numberValue(), nodeValue(), resultValue(), (AttemptOutcome) null, durationValue()));
    }

    @Test
    @DisplayName("rejects null for duration")
    @Tag("testId:consensusnode.client.Attempt/duration/rejects-null")
    void shouldRejectNullDuration() {
        assertThrows(NullPointerException.class, () -> new Attempt(numberValue(), nodeValue(), resultValue(), outcomeValue(), (Duration) null));
    }

    private static int numberValue() {
        return 1;
    }

    private static ConsensusNode nodeValue() {
        return new ConsensusNode(new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1}), 35211, new AccountId(0L, 0L, "", 3L, null, null));
    }

    private static AttemptResult resultValue() {
        return new NodeAnswer(BasicTransactionStatus.OK);
    }

    private static AttemptOutcome outcomeValue() {
        return AttemptOutcome.SUCCESS;
    }

    private static Duration durationValue() {
        return Duration.ofSeconds(1L);
    }

    private static Attempt create() {
        return new Attempt(numberValue(), nodeValue(), resultValue(), outcomeValue(), durationValue());
    }
}
