package org.hiero.mirrornode.transaction;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `CustomFeeLimit`, generated from the specs.
@DisplayName("CustomFeeLimit")
class CustomFeeLimitTest {

    @Test
    @DisplayName("creates a CustomFeeLimit and returns the values")
    @Tag("testId:mirrornode.transaction.CustomFeeLimit/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable AccountId accountId = accountIdValue();
        final long amount = amountValue();
        final @Nullable Address denominatingTokenId = denominatingTokenIdValue();
        final CustomFeeLimit subject = new CustomFeeLimit(accountId, amount, denominatingTokenId);
        assertValue(accountId, subject.accountId());
        assertValue(amount, subject.amount());
        assertValue(denominatingTokenId, subject.denominatingTokenId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.transaction.CustomFeeLimit/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable AccountId accountId = accountIdValue();
        final long amount = amountValue();
        final @Nullable Address denominatingTokenId = denominatingTokenIdValue();
        final CustomFeeLimit subject = CustomFeeLimit.builder()
                .accountId(accountId)
                .amount(amount)
                .denominatingTokenId(denominatingTokenId)
                .build();
        assertValue(accountId, subject.accountId());
        assertValue(amount, subject.amount());
        assertValue(denominatingTokenId, subject.denominatingTokenId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.transaction.CustomFeeLimit/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final CustomFeeLimit subject = CustomFeeLimit.builder()
                .amount(amountValue())
                .build();
        assertNull(subject.accountId());
        assertNull(subject.denominatingTokenId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.transaction.CustomFeeLimit/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> CustomFeeLimit.builder().build());
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.transaction.CustomFeeLimit/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final CustomFeeLimit subject = new CustomFeeLimit(accountIdValue(), value, denominatingTokenIdValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.transaction.CustomFeeLimit/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final CustomFeeLimit subject = new CustomFeeLimit(accountIdValue(), value, denominatingTokenIdValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.transaction.CustomFeeLimit/value-semantics")
    void shouldCompareByValue() {
        final CustomFeeLimit subject = create();
        final CustomFeeLimit other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long amountValue() {
        return 1L;
    }

    private static Address denominatingTokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static CustomFeeLimit create() {
        return new CustomFeeLimit(accountIdValue(), amountValue(), denominatingTokenIdValue());
    }
}
