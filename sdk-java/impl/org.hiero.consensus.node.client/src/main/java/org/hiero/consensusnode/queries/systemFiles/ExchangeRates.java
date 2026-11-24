package org.hiero.consensusnode.queries.systemFiles;

import java.util.Objects;
import org.hiero.nativeToken.ExchangeRate;

/// The current and the next exchange rate of the native token, as stored in the exchange rate file of the network.
/// Returned by `ExchangeRatesQuery`.
///
/// @param currentRate the rate that is valid now
/// @param nextRate the rate that becomes valid when `currentRate` expires
public record ExchangeRates(ExchangeRate currentRate, ExchangeRate nextRate) {

    /// Creates a new `ExchangeRates`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ExchangeRates {
        Objects.requireNonNull(currentRate, "currentRate must not be null");
        Objects.requireNonNull(nextRate, "nextRate must not be null");
    }
}
