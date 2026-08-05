// Generated from spec/base/native-token.md (nativeToken#NativeToken); DO NOT EDIT.

package org.hiero.sdk.v3.nativetoken;

/** A native-token amount preserving its concrete token and unit types. */
public interface NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> {
    /** Returns the amount expressed in {@link #unit()}. */
    long amount();
    /** Returns the amount unit. */
    Unit unit();
    /** Converts this amount to another unit. */
    Self to(Unit targetUnit);
    /** Returns the total number of smallest units. */
    long toBaseUnits();
}
