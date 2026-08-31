// Source: spec/base/native-token.md (nativeToken.ExchangeRate).

package org.hiero.sdk.v3.nativetoken;

import java.time.ZonedDateTime;
import java.util.Objects;

/** A native-token exchange rate in US dollar cents. */
public abstract class ExchangeRate {
    private final ZonedDateTime expirationTime;
    private final double exchangeRateInUsdCents;

    /** Creates structurally valid exchange-rate data without consulting a clock. */
    protected ExchangeRate(ZonedDateTime expirationTime, double exchangeRateInUsdCents) {
        this.expirationTime = Objects.requireNonNull(expirationTime, "expirationTime");
        this.exchangeRateInUsdCents = exchangeRateInUsdCents;
    }

    /** Returns the expiration instant with zone information. */
    public final ZonedDateTime expirationTime() { return expirationTime; }
    /** Returns the rate in US dollar cents. */
    public final double exchangeRateInUsdCents() { return exchangeRateInUsdCents; }
    /** Observes whether this rate has expired using provider-defined clock policy. */
    public abstract boolean isExpired();
}
