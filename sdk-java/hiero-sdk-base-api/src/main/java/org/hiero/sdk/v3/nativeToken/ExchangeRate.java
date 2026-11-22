package org.hiero.sdk.v3.nativeToken;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * An immutable native-token exchange rate expressed in USD cents with a zoned expiration time.
 *
 * <p>This abstraction preserves the supplied expiration time, including its zone, and the supplied
 * double-precision value. It imposes no numeric bounds or restriction on past expiration times.
 * Providers supply expiration evaluation and choose their current-time source; this API performs no
 * fetching, refreshing, caching, or currency or token conversion.
 */
public abstract class ExchangeRate {
    private final @NonNull ZonedDateTime expirationTime;
    private final double exchangeRateInUsdCents;

    /**
     * Creates an exchange rate with immutable expiration and USD-cent state.
     *
     * @param expirationTime the required expiration date and time, including its zone
     * @param exchangeRateInUsdCents the double-precision exchange value expressed in USD cents
     * @throws NullPointerException if {@code expirationTime} is {@code null}
     */
    protected ExchangeRate(@NonNull final ZonedDateTime expirationTime, final double exchangeRateInUsdCents) {
        this.expirationTime = Objects.requireNonNull(expirationTime, "expirationTime must not be null");
        this.exchangeRateInUsdCents = exchangeRateInUsdCents;
    }

    /**
     * Returns the stored expiration date and time without normalizing its zone.
     *
     * @return the immutable expiration date and time, including the supplied zone
     */
    public final @NonNull ZonedDateTime expirationTime() {
        return expirationTime;
    }

    /**
     * Returns the stored double-precision exchange value expressed in USD cents.
     *
     * @return the immutable exchange value in USD cents
     */
    public final double exchangeRateInUsdCents() {
        return exchangeRateInUsdCents;
    }

    /**
     * Reports whether the current instant is strictly later than the expiration instant.
     *
     * <p>Providers must compare instants rather than local date-time fields. Equality returns
     * {@code false}, including when the same instant is represented in different time zones.
     * Providers choose the current-time source and implement this evaluation.
     *
     * @return {@code true} only when the current instant is strictly after the expiration instant
     */
    public abstract boolean isExpired();
}
