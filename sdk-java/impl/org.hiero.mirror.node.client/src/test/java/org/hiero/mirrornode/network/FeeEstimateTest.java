package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `FeeEstimate`, generated from the specs.
@DisplayName("FeeEstimate")
class FeeEstimateTest {

    @Test
    @DisplayName("creates a FeeEstimate and returns the values")
    void shouldCreateAndReturnTheValues() {
        final long estimatedFee = estimatedFeeValue();
        final FeeEstimate subject = new FeeEstimate(estimatedFee);
        assertValue(estimatedFee, subject.estimatedFee());
    }

    @Test
    @DisplayName("estimatedFee: accepts the minimum (-9223372036854775808)")
    void shouldAcceptEstimatedFeeAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeEstimate subject = new FeeEstimate(value);
        assertValue(value, subject.estimatedFee());
    }

    @Test
    @DisplayName("estimatedFee: accepts the maximum (9223372036854775807)")
    void shouldAcceptEstimatedFeeAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeEstimate subject = new FeeEstimate(value);
        assertValue(value, subject.estimatedFee());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final FeeEstimate subject = create();
        final FeeEstimate other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long estimatedFeeValue() {
        return 1L;
    }

    private static FeeEstimate create() {
        return new FeeEstimate(estimatedFeeValue());
    }
}
