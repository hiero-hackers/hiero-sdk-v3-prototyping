package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ExchangeRate`, generated from the specs.
@DisplayName("ExchangeRate")
class ExchangeRateTest {

    @Test
    @DisplayName("creates an ExchangeRate and returns the values")
    @Tag("testId:mirrornode.network.ExchangeRate/create")
    void shouldCreateAndReturnTheValues() {
        final int centEquivalent = centEquivalentValue();
        final int hbarEquivalent = hbarEquivalentValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final ExchangeRate subject = new ExchangeRate(centEquivalent, hbarEquivalent, expirationTime);
        assertValue(centEquivalent, subject.centEquivalent());
        assertValue(hbarEquivalent, subject.hbarEquivalent());
        assertValue(expirationTime, subject.expirationTime());
    }

    @Test
    @DisplayName("centEquivalent: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.network.ExchangeRate/centEquivalent/accepts-at-minimum")
    void shouldAcceptCentEquivalentAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ExchangeRate subject = new ExchangeRate(value, hbarEquivalentValue(), expirationTimeValue());
        assertValue(value, subject.centEquivalent());
    }

    @Test
    @DisplayName("centEquivalent: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.network.ExchangeRate/centEquivalent/accepts-at-maximum")
    void shouldAcceptCentEquivalentAtMaximum() {
        final int value = 2147483647;
        final ExchangeRate subject = new ExchangeRate(value, hbarEquivalentValue(), expirationTimeValue());
        assertValue(value, subject.centEquivalent());
    }

    @Test
    @DisplayName("hbarEquivalent: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.network.ExchangeRate/hbarEquivalent/accepts-at-minimum")
    void shouldAcceptHbarEquivalentAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ExchangeRate subject = new ExchangeRate(centEquivalentValue(), value, expirationTimeValue());
        assertValue(value, subject.hbarEquivalent());
    }

    @Test
    @DisplayName("hbarEquivalent: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.network.ExchangeRate/hbarEquivalent/accepts-at-maximum")
    void shouldAcceptHbarEquivalentAtMaximum() {
        final int value = 2147483647;
        final ExchangeRate subject = new ExchangeRate(centEquivalentValue(), value, expirationTimeValue());
        assertValue(value, subject.hbarEquivalent());
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    @Tag("testId:mirrornode.network.ExchangeRate/expirationTime/rejects-null")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new ExchangeRate(centEquivalentValue(), hbarEquivalentValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.ExchangeRate/value-semantics")
    void shouldCompareByValue() {
        final ExchangeRate subject = create();
        final ExchangeRate other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static int centEquivalentValue() {
        return 1;
    }

    private static int hbarEquivalentValue() {
        return 1;
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ExchangeRate create() {
        return new ExchangeRate(centEquivalentValue(), hbarEquivalentValue(), expirationTimeValue());
    }
}
