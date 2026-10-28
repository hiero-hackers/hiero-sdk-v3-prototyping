package org.hiero.sdk.v3.nativeToken.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.sdk.v3.nativeToken.ExchangeRate;
import org.junit.jupiter.api.Test;

/** Provider-contract examples using a fixed clock; they are not a production expiration implementation. */
class ExchangeRateContractTest {
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void exposesExpirationTimeWithZoneAndRate() {
        final var expiration = ZonedDateTime.of(2026, 5, 6, 7, 8, 9, 10, ZoneId.of("Asia/Tokyo"));

        final var rate = new TestRate(expiration, 12.5);

        assertThat(rate.expirationTime()).isEqualTo(expiration);
        assertThat(rate.expirationTime().getZone()).isEqualTo(ZoneId.of("Asia/Tokyo"));
        assertThat(rate.exchangeRateInUsdCents()).isEqualTo(12.5);
    }

    @Test
    void rejectsNullExpirationTimeWithDescriptiveMessage() {
        assertThatNullPointerException()
                .isThrownBy(() -> new TestRate(null, 1))
                .withMessage("expirationTime");
    }

    @Test
    void doesNotInventValidationRules() {
        final var past = NOW.minusSeconds(1_000_000).atZone(ZoneOffset.UTC);

        assertThat(new TestRate(past, 0).exchangeRateInUsdCents()).isZero();
        assertThat(new TestRate(past, -3.5).exchangeRateInUsdCents()).isEqualTo(-3.5);
        assertThat(new TestRate(past, Double.NaN).exchangeRateInUsdCents()).isNaN();
        assertThat(new TestRate(past, Double.POSITIVE_INFINITY).exchangeRateInUsdCents()).isInfinite();
        assertThat(new TestRate(past, 1).expirationTime()).isEqualTo(past);
    }

    @Test
    void isExpiredWhenExpirationIsBeforeNow() {
        assertThat(rateExpiringAt(NOW.minusNanos(1)).isExpired()).isTrue();
        assertThat(rateExpiringAt(NOW.minusSeconds(3600)).isExpired()).isTrue();
    }

    @Test
    void isNotExpiredWhenExpirationEqualsNow() {
        assertThat(rateExpiringAt(NOW).isExpired()).isFalse();
    }

    @Test
    void isNotExpiredWhenExpirationIsAfterNow() {
        assertThat(rateExpiringAt(NOW.plusNanos(1)).isExpired()).isFalse();
    }

    @Test
    void equalInstantsInDifferentZonesAreNotExpired() {
        final var tokyo = new TestRate(NOW.atZone(ZoneId.of("Asia/Tokyo")), 1);
        final var newYork = new TestRate(NOW.atZone(ZoneId.of("America/New_York")), 1);

        assertThat(tokyo.isExpired()).isFalse();
        assertThat(newYork.isExpired()).isFalse();
    }

    private static TestRate rateExpiringAt(final Instant instant) {
        return new TestRate(instant.atZone(ZoneId.of("Europe/Berlin")), 1);
    }

    private static final class TestRate extends ExchangeRate {
        TestRate(final ZonedDateTime expirationTime, final double cents) {
            super(expirationTime, cents);
        }

        @Override
        public boolean isExpired() {
            return CLOCK.instant().isAfter(expirationTime().toInstant());
        }
    }
}
