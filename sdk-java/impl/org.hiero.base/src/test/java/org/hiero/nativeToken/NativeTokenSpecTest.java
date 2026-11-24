package org.hiero.nativeToken;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.hedera.HbarUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `native-token`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("native-token")
class NativeTokenSpecTest {

    /// `nativeToken/an-exchange-rate-keeps-its-two-numbers-exactly`
    ///
    /// Source: the Java SDK (ExchangeRatesTest, the current rate of its exchange rate file
    /// 0a1008b0ea0110b6b4231a0608f0bade9006121008b0ea01108cef231a060880d7de9006): 580150 cents for 30000 HBAR until
    /// epoch second 1645714800. The Swift SDK (ExchangeRatesUnitTests) decodes the same values.
    @Test
    @DisplayName("an exchange rate keeps its two numbers exactly")
    @Tag("testId:nativeToken/an-exchange-rate-keeps-its-two-numbers-exactly")
    void anExchangeRateKeepsItsTwoNumbersExactly() {
        // when
        final ExchangeRate rate = new ExchangeRate(30000, HbarUnit.HBAR, 580150, toZonedDateTime("2022-02-24T15:00:00Z"));

        // then
        assertEquals(30000, rate.nativeTokenEquivalent());
        assertEquals("ℏ", rate.nativeTokenUnit().symbol());
        assertEquals(100_000_000L, rate.nativeTokenUnit().baseUnitFactor());
        assertEquals(580150, rate.centEquivalent());
        assertEquals(toZonedDateTime("2022-02-24T15:00:00Z"), rate.expirationTime());
    }

    /// `nativeToken/an-exchange-rate-expires-after-its-expiration-time`
    ///
    /// Source: this spec (a rate has expired if the point in time is after its expiration time; at the expiration time
    /// it is still valid).
    @Test
    @DisplayName("an exchange rate expires after its expiration time")
    @Tag("testId:nativeToken/an-exchange-rate-expires-after-its-expiration-time")
    void anExchangeRateExpiresAfterItsExpirationTime() {
        // given
        final ExchangeRate rate = new ExchangeRate(30000, HbarUnit.HBAR, 580150, toZonedDateTime("2022-02-24T15:00:00Z"));

        // then
        assertFalse(rate.isExpiredAt(toZonedDateTime("2022-02-24T14:59:59Z")));
        assertFalse(rate.isExpiredAt(toZonedDateTime("2022-02-24T15:00:00Z")));
        assertTrue(rate.isExpiredAt(toZonedDateTime("2022-02-24T15:00:01Z")));
    }
}
