package org.hiero.consensusnode.queries.systemFiles;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.hedera.HbarUnit;
import org.hiero.nativeToken.ExchangeRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ExchangeRates`, generated from the specs.
@DisplayName("ExchangeRates")
class ExchangeRatesTest {

    @Test
    @DisplayName("creates an ExchangeRates and returns the values")
    @Tag("testId:consensusnode.queries.systemFiles.ExchangeRates/create")
    void shouldCreateAndReturnTheValues() {
        final ExchangeRate currentRate = currentRateValue();
        final ExchangeRate nextRate = nextRateValue();
        final ExchangeRates subject = new ExchangeRates(currentRate, nextRate);
        assertValue(currentRate, subject.currentRate());
        assertValue(nextRate, subject.nextRate());
    }

    @Test
    @DisplayName("rejects null for currentRate")
    @Tag("testId:consensusnode.queries.systemFiles.ExchangeRates/currentRate/rejects-null")
    void shouldRejectNullCurrentRate() {
        assertThrows(NullPointerException.class, () -> new ExchangeRates((ExchangeRate) null, nextRateValue()));
    }

    @Test
    @DisplayName("rejects null for nextRate")
    @Tag("testId:consensusnode.queries.systemFiles.ExchangeRates/nextRate/rejects-null")
    void shouldRejectNullNextRate() {
        assertThrows(NullPointerException.class, () -> new ExchangeRates(currentRateValue(), (ExchangeRate) null));
    }

    private static ExchangeRate currentRateValue() {
        return new ExchangeRate(30000, HbarUnit.HBAR, 580150, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static ExchangeRate nextRateValue() {
        return new ExchangeRate(30000, HbarUnit.HBAR, 580150, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static ExchangeRates create() {
        return new ExchangeRates(currentRateValue(), nextRateValue());
    }
}
