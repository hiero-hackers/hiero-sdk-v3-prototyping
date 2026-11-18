package org.hiero.sdk.v3.nativeToken;

import org.jspecify.annotations.NonNull;

/**
 * An immutable unit of a native token, implementable by enums or other unit providers.
 *
 * <p>Providers must retain a non-null symbol and an unchanging base-unit factor for the lifetime of
 * each unit. This contract does not select numeric validation rules for that factor.
 */
public interface NativeTokenUnit {
    /**
     * Returns the immutable display symbol of this unit.
     *
     * @return the non-null display symbol
     */
    @NonNull String symbol();

    /**
     * Returns the number of smallest indivisible base units contained in one of this unit.
     *
     * @return the immutable base-unit factor
     */
    long baseUnitFactor();
}
