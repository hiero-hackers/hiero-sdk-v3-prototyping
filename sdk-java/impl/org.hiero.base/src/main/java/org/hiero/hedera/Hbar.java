package org.hiero.hedera;

import java.math.BigInteger;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// HBAR, the native token of the Hedera network. `amount`, `unit`, and `to(...)` are inherited from
/// NativeToken.
public class Hbar extends NativeToken<Hbar, HbarUnit> {

    /// Creates a new `Hbar`.
    public Hbar(final long amount, final HbarUnit unit) {
        super(amount, unit);
    }

    /// Converts this amount to a different unit of the same token. Fails if the amount is not a whole number of the
    /// target unit (a conversion never loses a part of the amount) or does not fit into an int64 in that unit.
    ///
    /// @throws IllegalArgumentException if an invalid argument error occurs
    @Override
    public Hbar to(final HbarUnit targetUnit) {
        final BigInteger[] division = exactTinybars().divideAndRemainder(BigInteger.valueOf(targetUnit.baseUnitFactor()));
        if (division[1].signum() != 0) {
            throw new IllegalArgumentException(this + " is not a whole number of " + targetUnit.symbol());
        }
        return new Hbar(longValue(division[0], targetUnit), targetUnit);
    }

    /// Total amount expressed in base (smallest) units. Fails if the amount does not fit into an int64 of base units.
    ///
    /// @throws IllegalArgumentException if an invalid argument error occurs
    @Override
    public long toBaseUnits() {
        return longValue(exactTinybars(), HbarUnit.TINYBAR);
    }

    /// Total amount in tinybars (the base unit of HBAR); equivalent to toBaseUnits(). Fails if the amount does not fit
    /// into an int64 of tinybars.
    ///
    /// @throws IllegalArgumentException if an invalid argument error occurs
    public long toTinybars() {
        return toBaseUnits();
    }

    /// Two amounts of HBAR are equal if they are the same number of tinybars, whatever their units: 1 ℏ equals
    /// 100,000,000 tℏ. The amounts are compared exactly, also those that do not fit into an int64 of tinybars.
    @Override
    public boolean equals(final @Nullable Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Hbar hbar && exactTinybars().equals(hbar.exactTinybars());
    }

    /// Consistent with equals: equal amounts in different units have the same hash code.
    @Override
    public int hashCode() {
        return exactTinybars().hashCode();
    }

    @Override
    public String toString() {
        return "Hbar[amount=" + amount()
                + ", unit=" + unit()
                + "]";
    }

    /// The amount in tinybars without overflow.
    private BigInteger exactTinybars() {
        return BigInteger.valueOf(amount()).multiply(BigInteger.valueOf(unit().baseUnitFactor()));
    }

    private long longValue(final BigInteger value, final HbarUnit targetUnit) {
        try {
            return value.longValueExact();
        } catch (final ArithmeticException e) {
            throw new IllegalArgumentException(this + " does not fit into an int64 of " + targetUnit.symbol(), e);
        }
    }
}
