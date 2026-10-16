package org.hiero.nativeToken;

import java.util.Objects;

/// An amount of a network's native token, expressed in a given unit.
/// $$Self is the concrete token type (e.g. Hbar), so `to(...)` returns that exact type rather than the abstraction.
public abstract class NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> {

    private final long amount;
    private final Unit unit;

    /// Creates a new `NativeToken`.
    ///
    /// @throws NullPointerException if a required value is `null`
    protected NativeToken(final long amount, final Unit unit) {
        this.amount = amount;
        Objects.requireNonNull(unit, "unit must not be null");
        this.unit = unit;
    }

    /// amount expressed in `unit`
    public long amount() {
        return amount;
    }

    /// the unit `amount` is expressed in
    public Unit unit() {
        return unit;
    }

    /// Converts this amount to a different unit of the same token. Fails if the amount is not a whole number of the
    /// target unit (a conversion never loses a part of the amount) or does not fit into an int64 in that unit.
    ///
    /// @throws IllegalArgumentException if an invalid argument error occurs
    public abstract Self to(final Unit targetUnit);

    /// Total amount expressed in base (smallest) units. Fails if the amount does not fit into an int64 of base units.
    ///
    /// @throws IllegalArgumentException if an invalid argument error occurs
    public abstract long toBaseUnits();
}
