package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `RoyaltyFee`, generated from the specs.
@DisplayName("RoyaltyFee")
class RoyaltyFeeTest {

    @Test
    @DisplayName("creates a RoyaltyFee and returns the values")
    @Tag("testId:mirrornode.token.RoyaltyFee/create")
    void shouldCreateAndReturnTheValues() {
        final long numeratorAmount = numeratorAmountValue();
        final long denominatorAmount = denominatorAmountValue();
        final @Nullable Long fallbackFeeAmount = fallbackFeeAmountValue();
        final @Nullable AccountId collectorAccountId = collectorAccountIdValue();
        final @Nullable Address denominatingTokenId = denominatingTokenIdValue();
        final boolean allCollectorsAreExempt = allCollectorsAreExemptValue();
        final RoyaltyFee subject = new RoyaltyFee(numeratorAmount, denominatorAmount, fallbackFeeAmount, collectorAccountId, denominatingTokenId, allCollectorsAreExempt);
        assertValue(numeratorAmount, subject.numeratorAmount());
        assertValue(denominatorAmount, subject.denominatorAmount());
        assertValue(fallbackFeeAmount, subject.fallbackFeeAmount());
        assertValue(collectorAccountId, subject.collectorAccountId());
        assertValue(denominatingTokenId, subject.denominatingTokenId());
        assertValue(allCollectorsAreExempt, subject.allCollectorsAreExempt());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.token.RoyaltyFee/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long numeratorAmount = numeratorAmountValue();
        final long denominatorAmount = denominatorAmountValue();
        final @Nullable Long fallbackFeeAmount = fallbackFeeAmountValue();
        final @Nullable AccountId collectorAccountId = collectorAccountIdValue();
        final @Nullable Address denominatingTokenId = denominatingTokenIdValue();
        final boolean allCollectorsAreExempt = allCollectorsAreExemptValue();
        final RoyaltyFee subject = RoyaltyFee.builder()
                .numeratorAmount(numeratorAmount)
                .denominatorAmount(denominatorAmount)
                .fallbackFeeAmount(fallbackFeeAmount)
                .collectorAccountId(collectorAccountId)
                .denominatingTokenId(denominatingTokenId)
                .allCollectorsAreExempt(allCollectorsAreExempt)
                .build();
        assertValue(numeratorAmount, subject.numeratorAmount());
        assertValue(denominatorAmount, subject.denominatorAmount());
        assertValue(fallbackFeeAmount, subject.fallbackFeeAmount());
        assertValue(collectorAccountId, subject.collectorAccountId());
        assertValue(denominatingTokenId, subject.denominatingTokenId());
        assertValue(allCollectorsAreExempt, subject.allCollectorsAreExempt());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.token.RoyaltyFee/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final RoyaltyFee subject = RoyaltyFee.builder()
                .numeratorAmount(numeratorAmountValue())
                .denominatorAmount(denominatorAmountValue())
                .allCollectorsAreExempt(allCollectorsAreExemptValue())
                .build();
        assertNull(subject.fallbackFeeAmount());
        assertNull(subject.collectorAccountId());
        assertNull(subject.denominatingTokenId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.token.RoyaltyFee/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> RoyaltyFee.builder().build());
    }

    @Test
    @DisplayName("numeratorAmount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.RoyaltyFee/numeratorAmount/accepts-at-minimum")
    void shouldAcceptNumeratorAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final RoyaltyFee subject = new RoyaltyFee(value, denominatorAmountValue(), fallbackFeeAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue());
        assertValue(value, subject.numeratorAmount());
    }

    @Test
    @DisplayName("numeratorAmount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.RoyaltyFee/numeratorAmount/accepts-at-maximum")
    void shouldAcceptNumeratorAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final RoyaltyFee subject = new RoyaltyFee(value, denominatorAmountValue(), fallbackFeeAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue());
        assertValue(value, subject.numeratorAmount());
    }

    @Test
    @DisplayName("denominatorAmount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.RoyaltyFee/denominatorAmount/accepts-at-minimum")
    void shouldAcceptDenominatorAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final RoyaltyFee subject = new RoyaltyFee(numeratorAmountValue(), value, fallbackFeeAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue());
        assertValue(value, subject.denominatorAmount());
    }

    @Test
    @DisplayName("denominatorAmount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.RoyaltyFee/denominatorAmount/accepts-at-maximum")
    void shouldAcceptDenominatorAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final RoyaltyFee subject = new RoyaltyFee(numeratorAmountValue(), value, fallbackFeeAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue());
        assertValue(value, subject.denominatorAmount());
    }

    @Test
    @DisplayName("fallbackFeeAmount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.RoyaltyFee/fallbackFeeAmount/accepts-at-minimum")
    void shouldAcceptFallbackFeeAmountAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final RoyaltyFee subject = new RoyaltyFee(numeratorAmountValue(), denominatorAmountValue(), value, collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue());
        assertValue(value, subject.fallbackFeeAmount());
    }

    @Test
    @DisplayName("fallbackFeeAmount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.RoyaltyFee/fallbackFeeAmount/accepts-at-maximum")
    void shouldAcceptFallbackFeeAmountAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final RoyaltyFee subject = new RoyaltyFee(numeratorAmountValue(), denominatorAmountValue(), value, collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue());
        assertValue(value, subject.fallbackFeeAmount());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.token.RoyaltyFee/value-semantics")
    void shouldCompareByValue() {
        final RoyaltyFee subject = create();
        final RoyaltyFee other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long numeratorAmountValue() {
        return 1L;
    }

    private static long denominatorAmountValue() {
        return 1L;
    }

    private static long fallbackFeeAmountValue() {
        return 1L;
    }

    private static AccountId collectorAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address denominatingTokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static boolean allCollectorsAreExemptValue() {
        return true;
    }

    private static RoyaltyFee create() {
        return new RoyaltyFee(numeratorAmountValue(), denominatorAmountValue(), fallbackFeeAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue());
    }
}
