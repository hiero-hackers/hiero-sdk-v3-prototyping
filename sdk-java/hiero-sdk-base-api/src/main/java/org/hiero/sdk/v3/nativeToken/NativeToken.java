package org.hiero.sdk.v3.nativeToken;

import java.util.Objects;

/**
 * An immutable amount of a network's native token, expressed in a given unit.
 *
 * <p>Conversion arithmetic is provider-defined. This contract does not select rounding, truncation, overflow, or
 * unit-factor validation policies.
 *
 * @param <SELF> the concrete token type, so that {@link #to(NativeTokenUnit)} returns that exact type
 * @param <UNIT> the unit type the token amount is expressed in
 */
public abstract class NativeToken<SELF extends NativeToken<SELF, UNIT>, UNIT extends NativeTokenUnit> {
    private final long amount;
    private final UNIT unit;

    /**
     * Creates a token amount.
     *
     * @param amount the amount expressed in {@code unit}; any {@code long} value is accepted
     * @param unit the unit the amount is expressed in
     * @throws NullPointerException if {@code unit} is {@code null}
     */
    protected NativeToken(final long amount, final UNIT unit) {
        this.amount = amount;
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    /**
     * Returns the amount expressed in {@link #unit()}.
     *
     * @return the amount expressed in the associated unit
     */
    public final long amount() {
        return amount;
    }

    /**
     * Returns the unit the amount is expressed in.
     *
     * @return the associated unit
     */
    public final UNIT unit() {
        return unit;
    }

    /**
     * Converts this amount to a different unit of the same token. This instance is not modified.
     *
     * <p>Providers define how fractional results and overflow are handled and must reject a {@code null} target.
     *
     * @param targetUnit the unit to convert to
     * @return a new token amount expressed in {@code targetUnit}
     */
    public abstract SELF to(UNIT targetUnit);

    /**
     * Returns the total amount expressed in base (smallest) units.
     *
     * <p>Providers define how overflow is handled.
     *
     * @return the total amount in base units
     */
    public abstract long toBaseUnits();
}
