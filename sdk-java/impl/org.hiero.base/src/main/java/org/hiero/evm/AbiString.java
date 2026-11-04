package org.hiero.evm;

import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;

/// A `string`.
public record AbiString(String value) implements AbiValue {

    /// Creates a new `AbiString`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AbiString {
        Objects.requireNonNull(value, "value must not be null");
    }

    /// The type of the value.
    @Override
    public AbiType abiType() {
        return AbiCodec.typeOf(this);
    }
}
