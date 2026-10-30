package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FeeEstimateNetwork`, generated from the specs.
@DisplayName("FeeEstimateNetwork")
class FeeEstimateNetworkTest {

    @Test
    @DisplayName("creates a FeeEstimateNetwork and returns the values")
    @Tag("testId:mirrornode.network.FeeEstimateNetwork/create")
    void shouldCreateAndReturnTheValues() {
        final int multiplier = multiplierValue();
        final long subtotal = subtotalValue();
        final FeeEstimateNetwork subject = new FeeEstimateNetwork(multiplier, subtotal);
        assertValue(multiplier, subject.multiplier());
        assertValue(subtotal, subject.subtotal());
    }

    @Test
    @DisplayName("multiplier: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.network.FeeEstimateNetwork/multiplier/accepts-at-minimum")
    void shouldAcceptMultiplierAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final FeeEstimateNetwork subject = new FeeEstimateNetwork(value, subtotalValue());
        assertValue(value, subject.multiplier());
    }

    @Test
    @DisplayName("multiplier: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.network.FeeEstimateNetwork/multiplier/accepts-at-maximum")
    void shouldAcceptMultiplierAtMaximum() {
        final int value = 2147483647;
        final FeeEstimateNetwork subject = new FeeEstimateNetwork(value, subtotalValue());
        assertValue(value, subject.multiplier());
    }

    @Test
    @DisplayName("subtotal: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeEstimateNetwork/subtotal/accepts-at-minimum")
    void shouldAcceptSubtotalAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeEstimateNetwork subject = new FeeEstimateNetwork(multiplierValue(), value);
        assertValue(value, subject.subtotal());
    }

    @Test
    @DisplayName("subtotal: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeEstimateNetwork/subtotal/accepts-at-maximum")
    void shouldAcceptSubtotalAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeEstimateNetwork subject = new FeeEstimateNetwork(multiplierValue(), value);
        assertValue(value, subject.subtotal());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.FeeEstimateNetwork/value-semantics")
    void shouldCompareByValue() {
        final FeeEstimateNetwork subject = create();
        final FeeEstimateNetwork other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static int multiplierValue() {
        return 1;
    }

    private static long subtotalValue() {
        return 1L;
    }

    private static FeeEstimateNetwork create() {
        return new FeeEstimateNetwork(multiplierValue(), subtotalValue());
    }
}
