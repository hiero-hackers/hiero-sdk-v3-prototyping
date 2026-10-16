package org.hiero.hedera;

import java.util.Objects;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;

/// HBAR, the native token of the Hedera network. `amount`, `unit`, and `to(...)` are inherited from
/// NativeToken.
public class Hbar extends NativeToken<Hbar, HbarUnit> {

    /// Creates a new `Hbar`.
    public Hbar(final long amount, final HbarUnit unit) {
        super(amount, unit);
    }

    /// Convert this amount to a different unit of the same token
    @Override
    public Hbar to(final HbarUnit targetUnit) {
        final long tinybars = toBaseUnits();
        if (tinybars % targetUnit.baseUnitFactor() != 0) {
            throw new IllegalArgumentException(this + " is not a whole number of " + targetUnit.symbol());
        }
        return new Hbar(tinybars / targetUnit.baseUnitFactor(), targetUnit);
    }

    /// Total amount expressed in base (smallest) units
    @Override
    public long toBaseUnits() {
        return Math.multiplyExact(amount(), unit().baseUnitFactor());
    }

    /// Total amount in tinybars (the base unit of HBAR); equivalent to toBaseUnits().
    public long toTinybars() {
        return toBaseUnits();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof Hbar other
            && amount() == other.amount()
            && Objects.equals(unit(), other.unit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount(), unit());
    }

    @Override
    public String toString() {
        return "Hbar[amount=" + amount()
                + ", unit=" + unit()
                + "]";
    }
}
