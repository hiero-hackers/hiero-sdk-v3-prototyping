package org.hiero.evm;

import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;

/// An `address`.
public record AbiAddress(EvmAddress value) implements AbiValue {

    /// Creates a new `AbiAddress`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AbiAddress {
        Objects.requireNonNull(value, "value must not be null");
    }

    /// The type of the value.
    @Override
    public AbiType abiType() {
        return AbiCodec.typeOf(this);
    }
}
