package org.hiero.nativeToken.internal;

import java.time.ZonedDateTime;
import org.hiero.nativeToken.ExchangeRate;

/// An exchange rate as the consensus node reports it in a receipt.
public final class DefaultExchangeRate extends ExchangeRate {

    /// Creates an exchange rate.
    ///
    /// @param expirationTime         the instant until which the rate is valid
    /// @param exchangeRateInUsdCents the rate in US cents
    public DefaultExchangeRate(final ZonedDateTime expirationTime, final double exchangeRateInUsdCents) {
        super(expirationTime, exchangeRateInUsdCents);
    }

    @Override
    public boolean isExpired() {
        return expirationTime().toInstant().isBefore(java.time.Instant.now());
    }
}
