package org.hiero.sdk.v3.nativeToken;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * An immutable amount of a native token expressed in an associated unit.
 *
 * <p>Providers supply conversions and must use immutable units. This abstraction owns the stored
 * state but supplies no conversion arithmetic, fractional-result policy, overflow policy, or
 * unit-factor validation policy. No amount bounds beyond signed 64-bit storage are imposed.
 *
 * @param <SELF> the concrete token type returned by conversions
 * @param <UNIT> the units supported by this token
 */
public abstract class NativeToken<SELF extends NativeToken<SELF, UNIT>, UNIT extends NativeTokenUnit> {
    private final long amount;
    private final @NonNull UNIT unit;

    /**
     * Creates a token amount in the supplied immutable unit.
     *
     * @param amount the amount expressed in {@code unit}
     * @param unit the associated immutable unit
     * @throws NullPointerException if {@code unit} is {@code null}
     */
    protected NativeToken(final long amount, @NonNull final UNIT unit) {
        this.amount = amount;
        this.unit = Objects.requireNonNull(unit, "unit must not be null");
    }

    /**
     * Returns the stored amount expressed in {@link #unit()}.
     *
     * @return the immutable amount
     */
    public final long amount() {
        return amount;
    }

    /**
     * Returns the immutable unit in which {@link #amount()} is expressed.
     *
     * @return the associated unit
     */
    public final @NonNull UNIT unit() {
        return unit;
    }

    /**
     * Converts this amount to a target unit of the same token.
     *
     * <p>Providers must return a new concrete token amount in {@code targetUnit}, preserve this
     * instance's state, and reject a null target with a descriptive null check. Production policies
     * for fractional results and overflow require a shared contract decision.
     *
     * @param targetUnit the required target unit
     * @return a new amount mathematically converted to the target unit
     * @throws NullPointerException if {@code targetUnit} is {@code null}
     */
    public abstract @NonNull SELF to(@NonNull UNIT targetUnit);

    /**
     * Returns the total amount expressed in the smallest indivisible base units.
     *
     * <p>Providers supply the arithmetic. Production overflow and unit-factor validation policies
     * require a shared contract decision.
     *
     * @return the total amount in base units
     */
    public abstract long toBaseUnits();
}
