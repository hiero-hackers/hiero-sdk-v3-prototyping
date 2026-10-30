package org.hiero.mirrornode.network;

import java.time.ZonedDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// The current and the next exchange rate between HBAR and US cents.
///
/// @param timestamp consensus time at which the rates were valid
public record ExchangeRates(
        ExchangeRate currentRate,
        ExchangeRate nextRate,
        @Nullable ZonedDateTime timestamp) {

    /// Creates a new `ExchangeRates`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ExchangeRates {
        Objects.requireNonNull(currentRate, "currentRate must not be null");
        Objects.requireNonNull(nextRate, "nextRate must not be null");
    }
}
