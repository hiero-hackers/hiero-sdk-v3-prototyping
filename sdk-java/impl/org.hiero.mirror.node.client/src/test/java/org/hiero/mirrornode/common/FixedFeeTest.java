package org.hiero.mirrornode.common;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FixedFee`, generated from the specs.
@DisplayName("FixedFee")
class FixedFeeTest {

    @Test
    @DisplayName("creates a FixedFee and returns the values")
    @Tag("testId:mirrornode.common.FixedFee/create")
    void shouldCreateAndReturnTheValues() {
        final long amount = amountValue();
        final @Nullable AccountId collectorAccountId = collectorAccountIdValue();
        final @Nullable Address denominatingTokenId = denominatingTokenIdValue();
        final FixedFee subject = new FixedFee(amount, collectorAccountId, denominatingTokenId);
        assertValue(amount, subject.amount());
        assertValue(collectorAccountId, subject.collectorAccountId());
        assertValue(denominatingTokenId, subject.denominatingTokenId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.common.FixedFee/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long amount = amountValue();
        final @Nullable AccountId collectorAccountId = collectorAccountIdValue();
        final @Nullable Address denominatingTokenId = denominatingTokenIdValue();
        final FixedFee subject = FixedFee.builder()
                .amount(amount)
                .collectorAccountId(collectorAccountId)
                .denominatingTokenId(denominatingTokenId)
                .build();
        assertValue(amount, subject.amount());
        assertValue(collectorAccountId, subject.collectorAccountId());
        assertValue(denominatingTokenId, subject.denominatingTokenId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.common.FixedFee/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FixedFee subject = FixedFee.builder()
                .amount(amountValue())
                .build();
        assertNull(subject.collectorAccountId());
        assertNull(subject.denominatingTokenId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.common.FixedFee/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> FixedFee.builder().build());
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.common.FixedFee/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final FixedFee subject = new FixedFee(value, collectorAccountIdValue(), denominatingTokenIdValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.common.FixedFee/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final FixedFee subject = new FixedFee(value, collectorAccountIdValue(), denominatingTokenIdValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.common.FixedFee/value-semantics")
    void shouldCompareByValue() {
        final FixedFee subject = create();
        final FixedFee other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long amountValue() {
        return 1L;
    }

    private static AccountId collectorAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address denominatingTokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static FixedFee create() {
        return new FixedFee(amountValue(), collectorAccountIdValue(), denominatingTokenIdValue());
    }
}
