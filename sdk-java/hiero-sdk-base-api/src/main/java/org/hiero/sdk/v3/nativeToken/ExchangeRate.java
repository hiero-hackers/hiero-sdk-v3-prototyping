package org.hiero.sdk.v3.nativeToken;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * An immutable exchange rate of a native token, expressed in USD cents, together with its expiration time.
 *
 * <p>This contract owns the immutable state only. Expiration evaluation, clock selection, and fetching or refreshing
 * rates are provider concerns. No bounds are imposed on the rate value or the expiration time.
 */
public abstract class ExchangeRate {
    private final ZonedDateTime expirationTime;
    private final double exchangeRateInUsdCents;

    /**
     * Creates an exchange rate.
     *
     * @param expirationTime the time-zone-aware expiration time; the supplied zone is preserved
     * @param exchangeRateInUsdCents the exchange rate of the native token in USD cents; any {@code double} is accepted
     * @throws NullPointerException if {@code expirationTime} is {@code null}
     */
    protected ExchangeRate(final ZonedDateTime expirationTime, final double exchangeRateInUsdCents) {
        this.expirationTime = Objects.requireNonNull(expirationTime, "expirationTime");
        this.exchangeRateInUsdCents = exchangeRateInUsdCents;
    }

    /**
     * Returns the expiration time of this exchange rate.
     *
     * @return the expiration time, in the zone it was supplied with
     */
    public final ZonedDateTime expirationTime() {
        return expirationTime;
    }

    /**
     * Returns the exchange rate of the native token in USD cents.
     *
     * @return the exchange rate in USD cents
     */
    public final double exchangeRateInUsdCents() {
        return exchangeRateInUsdCents;
    }

    /**
     * Reports whether this exchange rate has expired.
     *
     * <p>Providers must compare the current instant with the instant of {@link #expirationTime()} and return
     * {@code true} only when the current instant is strictly later. When the two instants are equal the result is
     * {@code false}, regardless of the time zones involved.
     *
     * @return {@code true} if the current time is strictly later than the expiration time
     */
    public abstract boolean isExpired();
}
