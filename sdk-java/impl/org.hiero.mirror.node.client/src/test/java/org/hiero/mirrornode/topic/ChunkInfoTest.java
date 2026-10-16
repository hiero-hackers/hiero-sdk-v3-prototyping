package org.hiero.mirrornode.topic;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `ChunkInfo`, generated from the specs.
@DisplayName("ChunkInfo")
class ChunkInfoTest {

    @Test
    @DisplayName("creates a ChunkInfo and returns the values")
    void shouldCreateAndReturnTheValues() {
        final TransactionId initialTransactionId = initialTransactionIdValue();
        final int nonce = nonceValue();
        final int number = numberValue();
        final int total = totalValue();
        final boolean scheduled = scheduledValue();
        final ChunkInfo subject = new ChunkInfo(initialTransactionId, nonce, number, total, scheduled);
        assertValue(initialTransactionId, subject.initialTransactionId());
        assertValue(nonce, subject.nonce());
        assertValue(number, subject.number());
        assertValue(total, subject.total());
        assertValue(scheduled, subject.scheduled());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId initialTransactionId = initialTransactionIdValue();
        final int nonce = nonceValue();
        final int number = numberValue();
        final int total = totalValue();
        final boolean scheduled = scheduledValue();
        final ChunkInfo subject = ChunkInfo.builder()
                .initialTransactionId(initialTransactionId)
                .nonce(nonce)
                .number(number)
                .total(total)
                .scheduled(scheduled)
                .build();
        assertValue(initialTransactionId, subject.initialTransactionId());
        assertValue(nonce, subject.nonce());
        assertValue(number, subject.number());
        assertValue(total, subject.total());
        assertValue(scheduled, subject.scheduled());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final ChunkInfo subject = ChunkInfo.builder()
                .initialTransactionId(initialTransactionIdValue())
                .nonce(nonceValue())
                .number(numberValue())
                .total(totalValue())
                .scheduled(scheduledValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ChunkInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for initialTransactionId")
    void shouldRejectNullInitialTransactionId() {
        assertThrows(NullPointerException.class, () -> new ChunkInfo((TransactionId) null, nonceValue(), numberValue(), totalValue(), scheduledValue()));
    }

    @Test
    @DisplayName("nonce: accepts the minimum (-2147483648)")
    void shouldAcceptNonceAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ChunkInfo subject = new ChunkInfo(initialTransactionIdValue(), value, numberValue(), totalValue(), scheduledValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: accepts the maximum (2147483647)")
    void shouldAcceptNonceAtMaximum() {
        final int value = 2147483647;
        final ChunkInfo subject = new ChunkInfo(initialTransactionIdValue(), value, numberValue(), totalValue(), scheduledValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("number: accepts the minimum (-2147483648)")
    void shouldAcceptNumberAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ChunkInfo subject = new ChunkInfo(initialTransactionIdValue(), nonceValue(), value, totalValue(), scheduledValue());
        assertValue(value, subject.number());
    }

    @Test
    @DisplayName("number: accepts the maximum (2147483647)")
    void shouldAcceptNumberAtMaximum() {
        final int value = 2147483647;
        final ChunkInfo subject = new ChunkInfo(initialTransactionIdValue(), nonceValue(), value, totalValue(), scheduledValue());
        assertValue(value, subject.number());
    }

    @Test
    @DisplayName("total: accepts the minimum (-2147483648)")
    void shouldAcceptTotalAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ChunkInfo subject = new ChunkInfo(initialTransactionIdValue(), nonceValue(), numberValue(), value, scheduledValue());
        assertValue(value, subject.total());
    }

    @Test
    @DisplayName("total: accepts the maximum (2147483647)")
    void shouldAcceptTotalAtMaximum() {
        final int value = 2147483647;
        final ChunkInfo subject = new ChunkInfo(initialTransactionIdValue(), nonceValue(), numberValue(), value, scheduledValue());
        assertValue(value, subject.total());
    }

    private static TransactionId initialTransactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static int nonceValue() {
        return 1;
    }

    private static int numberValue() {
        return 1;
    }

    private static int totalValue() {
        return 1;
    }

    private static boolean scheduledValue() {
        return true;
    }

    private static ChunkInfo create() {
        return new ChunkInfo(initialTransactionIdValue(), nonceValue(), numberValue(), totalValue(), scheduledValue());
    }
}
