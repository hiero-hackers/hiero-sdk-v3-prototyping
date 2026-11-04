package org.hiero.evm;

import java.util.List;
import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;

/// A tuple of values: a struct, or the arguments or results of a function.
public record AbiTuple(List<AbiValue> values) implements AbiValue {

    /// Creates a new `AbiTuple`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AbiTuple {
        Objects.requireNonNull(values, "values must not be null");
        values = List.copyOf(values);
    }

    /// The type of the value.
    @Override
    public AbiType abiType() {
        return AbiCodec.typeOf(this);
    }

    /// Encodes the values, e.g. the arguments of a constructor (the call data of a function also has its selector).
    /// Throws if a value does not fit its type.
    ///
    /// @throws IllegalArgumentException if an invalid argument error occurs
    public byte[] encode() {
        return AbiCodec.encode(values);
    }
}
