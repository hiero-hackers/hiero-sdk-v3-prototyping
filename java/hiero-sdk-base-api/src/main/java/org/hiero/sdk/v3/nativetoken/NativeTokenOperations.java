// Generated from spec/base/native-token.md (nativeToken#NativeTokenOperations); DO NOT EDIT.

package org.hiero.sdk.v3.nativetoken;

/** Body-free native-token conversion and time-observation contracts. */
public interface NativeTokenOperations {
    /** Converts a token to another unit of its own unit family. */
    <S extends NativeToken<S, U>, U extends NativeTokenUnit> S convert(S token, U targetUnit);
    /** Returns a token amount in smallest units. */
    long toBaseUnits(NativeToken<?, ?> token);
    /** Observes exchange-rate expiration without defining clock policy. */
    boolean isExpired(ExchangeRate exchangeRate);
}
