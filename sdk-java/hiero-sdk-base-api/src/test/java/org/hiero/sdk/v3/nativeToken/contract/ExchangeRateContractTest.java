package org.hiero.sdk.v3.nativeToken.contract;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.hiero.sdk.v3.nativeToken.ExchangeRate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/** Fixed-clock provider examples, not a production implementation of expiration evaluation. */
class ExchangeRateContractTest {
    private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneId.of("America/New_York"));

    @Nested
    class State {
        @Test
        void observesStoredExpirationIncludingZoneAndUsdCentValue() {
            // GIVEN
            final ZonedDateTime expiration = NOW.atZone(ZoneId.of("Asia/Tokyo"));
            final ExchangeRate rate = new FixedClockRate(expiration, 12.75, CLOCK);
            // WHEN
            final ZonedDateTime observed = rate.expirationTime();
            final ZonedDateTime changed = observed.plusDays(1);
            // THEN
            assertThat(observed).isSameAs(expiration);
            assertThat(observed.getZone()).isEqualTo(ZoneId.of("Asia/Tokyo"));
            assertThat(changed).isNotEqualTo(expiration);
            assertThat(rate.expirationTime()).isSameAs(expiration);
            assertThat(rate.exchangeRateInUsdCents()).isEqualTo(12.75);
        }

        @Test
        void rejectsNullExpirationAtConstruction() {
            // GIVEN / WHEN / THEN
            assertThatNullPointerException().isThrownBy(() -> new FixedClockRate(null, 12.75, CLOCK))
                    .withMessage("expirationTime must not be null");
        }

        @ParameterizedTest
        @ValueSource(doubles = {Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -12.75, -0.0, 0.0,
                Double.MIN_VALUE, 12.75, Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN})
        void retainsDoubleValuesAndAllowsPastExpirationWithoutExtraValidation(final double value) {
            // GIVEN
            final ZonedDateTime past = NOW.minusSeconds(1).atZone(ZoneOffset.UTC);
            // WHEN
            final ExchangeRate rate = new FixedClockRate(past, value, CLOCK);
            // THEN: raw bits also distinguish positive and negative zero
            assertThat(Double.doubleToRawLongBits(rate.exchangeRateInUsdCents()))
                    .isEqualTo(Double.doubleToRawLongBits(value));
            assertThat(rate.expirationTime()).isSameAs(past);
        }
    }

    @Nested
    class Expiration {
        @ParameterizedTest
        @ValueSource(longs = {-1_000_000_000L, -1L})
        void expiresOnlyWhenCurrentInstantIsStrictlyLater(final long expirationOffsetNanos) {
            // GIVEN
            final ZonedDateTime expiration = NOW.plusNanos(expirationOffsetNanos).atZone(ZoneId.of("Asia/Tokyo"));
            final ExchangeRate rate = new FixedClockRate(expiration, 12.75, CLOCK);
            // WHEN / THEN
            assertThat(rate.isExpired()).isTrue();
            assertThat(rate.expirationTime()).isSameAs(expiration);
            assertThat(rate.exchangeRateInUsdCents()).isEqualTo(12.75);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, 1L, 1_000_000_000L})
        void doesNotExpireAtOrBeforeExpiration(final long expirationOffsetNanos) {
            // GIVEN
            final ZonedDateTime expiration = NOW.plusNanos(expirationOffsetNanos).atZone(ZoneId.of("Asia/Tokyo"));
            final ExchangeRate rate = new FixedClockRate(expiration, 12.75, CLOCK);
            // WHEN / THEN
            assertThat(rate.isExpired()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"UTC", "America/New_York", "Asia/Tokyo", "Pacific/Honolulu"})
        void equalInstantsDoNotExpireRegardlessOfZone(final String expirationZone) {
            // GIVEN
            final ZonedDateTime expiration = NOW.atZone(ZoneId.of(expirationZone));
            final ExchangeRate rate = new FixedClockRate(expiration, 12.75, CLOCK);
            // WHEN / THEN
            assertThat(rate.isExpired()).isFalse();
            assertThat(rate.expirationTime().getZone()).isEqualTo(ZoneId.of(expirationZone));
        }
    }

    private static final class FixedClockRate extends ExchangeRate {
        private final Clock clock;

        private FixedClockRate(final ZonedDateTime expirationTime, final double exchangeRateInUsdCents,
                final Clock clock) {
            super(expirationTime, exchangeRateInUsdCents);
            this.clock = Objects.requireNonNull(clock, "clock must not be null");
        }

        @Override
        public boolean isExpired() {
            return clock.instant().isAfter(expirationTime().toInstant());
        }
    }
}
