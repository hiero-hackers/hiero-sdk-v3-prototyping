package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FractionalFee`, generated from the specs.
@DisplayName("FractionalFee")
class FractionalFeeTest {

    @Test
    @DisplayName("creates a FractionalFee and returns the values")
    @Tag("testId:mirrornode.token.FractionalFee/create")
    void shouldCreateAndReturnTheValues() {
        final long numeratorAmount = numeratorAmountValue();
        final long denominatorAmount = denominatorAmountValue();
        final @Nullable AccountId collectorAccountId = collectorAccountIdValue();
        final @Nullable Address denominatingTokenId = denominatingTokenIdValue();
        final boolean allCollectorsAreExempt = allCollectorsAreExemptValue();
        final @Nullable Long maximum = maximumValue();
        final long minimum = minimumValue();
        final boolean netOfTransfers = netOfTransfersValue();
        final FractionalFee subject = new FractionalFee(numeratorAmount, denominatorAmount, collectorAccountId, denominatingTokenId, allCollectorsAreExempt, maximum, minimum, netOfTransfers);
        assertValue(numeratorAmount, subject.numeratorAmount());
        assertValue(denominatorAmount, subject.denominatorAmount());
        assertValue(collectorAccountId, subject.collectorAccountId());
        assertValue(denominatingTokenId, subject.denominatingTokenId());
        assertValue(allCollectorsAreExempt, subject.allCollectorsAreExempt());
        assertValue(maximum, subject.maximum());
        assertValue(minimum, subject.minimum());
        assertValue(netOfTransfers, subject.netOfTransfers());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.token.FractionalFee/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long numeratorAmount = numeratorAmountValue();
        final long denominatorAmount = denominatorAmountValue();
        final @Nullable AccountId collectorAccountId = collectorAccountIdValue();
        final @Nullable Address denominatingTokenId = denominatingTokenIdValue();
        final boolean allCollectorsAreExempt = allCollectorsAreExemptValue();
        final @Nullable Long maximum = maximumValue();
        final long minimum = minimumValue();
        final boolean netOfTransfers = netOfTransfersValue();
        final FractionalFee subject = FractionalFee.builder()
                .numeratorAmount(numeratorAmount)
                .denominatorAmount(denominatorAmount)
                .collectorAccountId(collectorAccountId)
                .denominatingTokenId(denominatingTokenId)
                .allCollectorsAreExempt(allCollectorsAreExempt)
                .maximum(maximum)
                .minimum(minimum)
                .netOfTransfers(netOfTransfers)
                .build();
        assertValue(numeratorAmount, subject.numeratorAmount());
        assertValue(denominatorAmount, subject.denominatorAmount());
        assertValue(collectorAccountId, subject.collectorAccountId());
        assertValue(denominatingTokenId, subject.denominatingTokenId());
        assertValue(allCollectorsAreExempt, subject.allCollectorsAreExempt());
        assertValue(maximum, subject.maximum());
        assertValue(minimum, subject.minimum());
        assertValue(netOfTransfers, subject.netOfTransfers());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.token.FractionalFee/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FractionalFee subject = FractionalFee.builder()
                .numeratorAmount(numeratorAmountValue())
                .denominatorAmount(denominatorAmountValue())
                .allCollectorsAreExempt(allCollectorsAreExemptValue())
                .minimum(minimumValue())
                .netOfTransfers(netOfTransfersValue())
                .build();
        assertNull(subject.collectorAccountId());
        assertNull(subject.denominatingTokenId());
        assertNull(subject.maximum());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.token.FractionalFee/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> FractionalFee.builder().build());
    }

    @Test
    @DisplayName("numeratorAmount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.FractionalFee/numeratorAmount/accepts-at-minimum")
    void shouldAcceptNumeratorAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final FractionalFee subject = new FractionalFee(value, denominatorAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), maximumValue(), minimumValue(), netOfTransfersValue());
        assertValue(value, subject.numeratorAmount());
    }

    @Test
    @DisplayName("numeratorAmount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.FractionalFee/numeratorAmount/accepts-at-maximum")
    void shouldAcceptNumeratorAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final FractionalFee subject = new FractionalFee(value, denominatorAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), maximumValue(), minimumValue(), netOfTransfersValue());
        assertValue(value, subject.numeratorAmount());
    }

    @Test
    @DisplayName("denominatorAmount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.FractionalFee/denominatorAmount/accepts-at-minimum")
    void shouldAcceptDenominatorAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final FractionalFee subject = new FractionalFee(numeratorAmountValue(), value, collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), maximumValue(), minimumValue(), netOfTransfersValue());
        assertValue(value, subject.denominatorAmount());
    }

    @Test
    @DisplayName("denominatorAmount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.FractionalFee/denominatorAmount/accepts-at-maximum")
    void shouldAcceptDenominatorAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final FractionalFee subject = new FractionalFee(numeratorAmountValue(), value, collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), maximumValue(), minimumValue(), netOfTransfersValue());
        assertValue(value, subject.denominatorAmount());
    }

    @Test
    @DisplayName("maximum: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.FractionalFee/maximum/accepts-at-minimum")
    void shouldAcceptMaximumAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final FractionalFee subject = new FractionalFee(numeratorAmountValue(), denominatorAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), value, minimumValue(), netOfTransfersValue());
        assertValue(value, subject.maximum());
    }

    @Test
    @DisplayName("maximum: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.FractionalFee/maximum/accepts-at-maximum")
    void shouldAcceptMaximumAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final FractionalFee subject = new FractionalFee(numeratorAmountValue(), denominatorAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), value, minimumValue(), netOfTransfersValue());
        assertValue(value, subject.maximum());
    }

    @Test
    @DisplayName("minimum: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.token.FractionalFee/minimum/accepts-at-minimum")
    void shouldAcceptMinimumAtMinimum() {
        final long value = -9223372036854775808L;
        final FractionalFee subject = new FractionalFee(numeratorAmountValue(), denominatorAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), maximumValue(), value, netOfTransfersValue());
        assertValue(value, subject.minimum());
    }

    @Test
    @DisplayName("minimum: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.token.FractionalFee/minimum/accepts-at-maximum")
    void shouldAcceptMinimumAtMaximum() {
        final long value = 9223372036854775807L;
        final FractionalFee subject = new FractionalFee(numeratorAmountValue(), denominatorAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), maximumValue(), value, netOfTransfersValue());
        assertValue(value, subject.minimum());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.token.FractionalFee/value-semantics")
    void shouldCompareByValue() {
        final FractionalFee subject = create();
        final FractionalFee other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long numeratorAmountValue() {
        return 1L;
    }

    private static long denominatorAmountValue() {
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

    private static long maximumValue() {
        return 1L;
    }

    private static long minimumValue() {
        return 1L;
    }

    private static boolean netOfTransfersValue() {
        return true;
    }

    private static FractionalFee create() {
        return new FractionalFee(numeratorAmountValue(), denominatorAmountValue(), collectorAccountIdValue(), denominatingTokenIdValue(), allCollectorsAreExemptValue(), maximumValue(), minimumValue(), netOfTransfersValue());
    }
}
