// Source: spec/base/native-token.md (nativeToken.NativeToken).

package org.hiero.sdk.v3.nativetoken;

import java.util.Objects;

/** A native-token amount preserving its concrete token and unit types. */
public abstract class NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> {
    private final long amount;
    private final Unit unit;

    /** Creates an amount expressed in a non-null unit. */
    protected NativeToken(long amount, Unit unit) {
        this.amount = amount;
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    /** Returns the amount expressed in {@link #unit()}. */
    public final long amount() { return amount; }
    /** Returns the amount unit. */
    public final Unit unit() { return unit; }
    /** Converts this amount to another unit. */
    public abstract Self to(Unit targetUnit);
    /** Returns the total number of smallest units. */
    public abstract long toBaseUnits();
}
