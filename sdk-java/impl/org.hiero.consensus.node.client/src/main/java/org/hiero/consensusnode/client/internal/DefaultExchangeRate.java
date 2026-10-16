package org.hiero.consensusnode.client.internal;

import java.time.Instant;
import java.time.ZonedDateTime;
import org.hiero.nativeToken.ExchangeRate;

/// The `ExchangeRate` of a receipt.
final class DefaultExchangeRate extends ExchangeRate {

    /// Creates an exchange rate.
    ///
    /// @param expirationTime         the end of its validity
    /// @param exchangeRateInUsdCents the price of one whole unit of the native token in US cents
    DefaultExchangeRate(final ZonedDateTime expirationTime, final double exchangeRateInUsdCents) {
        super(expirationTime, exchangeRateInUsdCents);
    }

    @Override
    public boolean isExpired() {
        return expirationTime().toInstant().isBefore(Instant.now());
    }
}
