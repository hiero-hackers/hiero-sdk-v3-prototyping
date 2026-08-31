// Generated from spec/base/native-token.md (nativeToken#ExchangeRate); DO NOT EDIT.

package org.hiero.sdk.v3.nativetoken;

import java.time.ZonedDateTime;

/** A native-token exchange rate in US dollar cents. */
public interface ExchangeRate {
    /** Returns the expiration instant with zone information. */
    ZonedDateTime expirationTime();
    /** Returns the rate in US dollar cents. */
    double exchangeRateInUsdCents();
    /** Observes whether this rate has expired. */
    boolean isExpired();
}
