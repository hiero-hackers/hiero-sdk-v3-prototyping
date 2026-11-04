package org.hiero.evm;

import org.hiero.evm.internal.AbiCodec;


/// A `bool`.
public record AbiBool(boolean value) implements AbiValue {

    /// The type of the value.
    @Override
    public AbiType abiType() {
        return AbiCodec.typeOf(this);
    }
}
