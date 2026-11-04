package org.hiero.evm;

import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;
import org.jspecify.annotations.Nullable;

/// An array type: `T[]` of any length, or `T[length]` of a fixed length.
///
/// @param elementType the type of the elements
/// @param length the number of elements of a fixed-size array; absent for T[]
public record AbiArrayType(AbiType elementType, @Nullable Long length) implements AbiType {

    /// Creates a new `AbiArrayType`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AbiArrayType {
        Objects.requireNonNull(elementType, "elementType must not be null");
        if (length != null && (length < 0L || length > 4294967295L)) {
            throw new IllegalArgumentException("length must be between 0 and 4294967295");
        }
        if (length != null && length < 1L) {
            throw new IllegalArgumentException("length must be at least 1");
        }
    }

    /// The canonical name of the type, as used in a signature: "uint256", "bytes32", "(address,uint256)[]".
    @Override
    public String toString() {
        return AbiCodec.name(this);
    }
}
