package org.hiero.sdk.v3.nativeToken;

/**
 * A unit of a native token, either the smallest indivisible unit or a named multiple of it.
 *
 * <p>Implementations must be immutable and must return the same values on every call. This contract is
 * deliberately small so that providers can implement it with an {@code enum}.
 */
public interface NativeTokenUnit {
    /**
     * Returns the display symbol of the unit.
     *
     * @return the display symbol of the unit
     */
    String symbol();

    /**
     * Returns the number of base (smallest) units contained in one of this unit.
     *
     * @return the base-unit factor
     */
    long baseUnitFactor();
}
