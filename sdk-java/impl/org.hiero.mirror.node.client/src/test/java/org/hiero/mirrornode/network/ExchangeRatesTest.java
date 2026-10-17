package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ExchangeRates`, generated from the specs.
@DisplayName("ExchangeRates")
class ExchangeRatesTest {

    @Test
    @DisplayName("creates an ExchangeRates and returns the values")
    @Tag("testId:mirrornode.network.ExchangeRates/create")
    void shouldCreateAndReturnTheValues() {
        final ExchangeRate currentRate = currentRateValue();
        final ExchangeRate nextRate = nextRateValue();
        final ExchangeRates subject = new ExchangeRates(currentRate, nextRate);
        assertValue(currentRate, subject.currentRate());
        assertValue(nextRate, subject.nextRate());
    }

    @Test
    @DisplayName("rejects null for currentRate")
    @Tag("testId:mirrornode.network.ExchangeRates/currentRate/rejects-null")
    void shouldRejectNullCurrentRate() {
        assertThrows(NullPointerException.class, () -> new ExchangeRates((ExchangeRate) null, nextRateValue()));
    }

    @Test
    @DisplayName("rejects null for nextRate")
    @Tag("testId:mirrornode.network.ExchangeRates/nextRate/rejects-null")
    void shouldRejectNullNextRate() {
        assertThrows(NullPointerException.class, () -> new ExchangeRates(currentRateValue(), (ExchangeRate) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.ExchangeRates/value-semantics")
    void shouldCompareByValue() {
        final ExchangeRates subject = create();
        final ExchangeRates other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static ExchangeRate currentRateValue() {
        return new ExchangeRate(1, 1, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static ExchangeRate nextRateValue() {
        return new ExchangeRate(1, 1, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static ExchangeRates create() {
        return new ExchangeRates(currentRateValue(), nextRateValue());
    }
}
