package org.hiero.nativeToken;

import java.time.ZonedDateTime;
import java.util.Objects;

/// The value of the native token in US cents for a limited period of time: `nativeTokenEquivalent` tokens in the
/// unit `nativeTokenUnit` are worth `centEquivalent` US cents until `expirationTime`. The rate is the fraction
/// centEquivalent / nativeTokenEquivalent (US cents per `nativeTokenUnit`); calculate with the two numbers as whole
/// numbers, never with a floating-point quotient.
///
/// @param nativeTokenEquivalent The denominator of the rate: the number of tokens, counted in `nativeTokenUnit`, whose value is
///   `centEquivalent` US cents. For Hedera the unit is HBAR, so 30000 means 30000 HBAR (3,000,000,000,000 tinybar).
///   To relate it to an amount in base units, multiply it by `nativeTokenUnit.baseUnitFactor`.
/// @param nativeTokenUnit The unit `nativeTokenEquivalent` is counted in: the unit the network quotes its rate in, not the base unit (for
///   Hedera: `HbarUnit.HBAR`). Its `baseUnitFactor` is the number of base units in one token of the rate.
/// @param centEquivalent The numerator of the rate: the value of `nativeTokenEquivalent` tokens in US cents (not dollars). With
///   `nativeTokenEquivalent` = 30000 and `centEquivalent` = 580150, 30000 HBAR are worth 580150 cents ($5,801.50),
///   i.e. 1 HBAR is worth about 19.34 cents.
/// @param expirationTime The point in time until which the rate is valid; after it, the next rate of the network applies.
public record ExchangeRate(
        int nativeTokenEquivalent,
        NativeTokenUnit nativeTokenUnit,
        int centEquivalent,
        ZonedDateTime expirationTime) {

    /// Creates a new `ExchangeRate`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public ExchangeRate {
        Objects.requireNonNull(nativeTokenUnit, "nativeTokenUnit must not be null");
        Objects.requireNonNull(expirationTime, "expirationTime must not be null");
    }

    /// Returns true if the rate has expired at the given point in time, i.e. if `time` is after `expirationTime`.
    public boolean isExpiredAt(final ZonedDateTime time) {
        Objects.requireNonNull(time, "time must not be null");
        return time.isAfter(expirationTime);
    }
}
