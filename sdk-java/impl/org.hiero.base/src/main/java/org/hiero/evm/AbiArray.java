package org.hiero.evm;

import java.util.List;
import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;

/// An array. Every value has the element type of `arrayType`, and a fixed-size array has exactly `arrayType.length`
/// values; both are checked when the array is encoded.
public record AbiArray(AbiArrayType arrayType, List<AbiValue> values) implements AbiValue {

    /// Creates a new `AbiArray`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AbiArray {
        Objects.requireNonNull(arrayType, "arrayType must not be null");
        Objects.requireNonNull(values, "values must not be null");
        values = List.copyOf(values);
    }

    /// The type of the value.
    @Override
    public AbiType abiType() {
        return AbiCodec.typeOf(this);
    }
}
