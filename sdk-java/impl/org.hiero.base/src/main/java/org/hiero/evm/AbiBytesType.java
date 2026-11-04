package org.hiero.evm;

import org.hiero.evm.internal.AbiCodec;
import org.jspecify.annotations.Nullable;


/// The type `bytes`: any number of bytes.
public final class AbiBytesType implements AbiType {

    /// Creates a new `AbiBytesType`.
    public AbiBytesType() {
    }

    /// The canonical name of the type, as used in a signature: "uint256", "bytes32", "(address,uint256)[]".
    @Override
    public String toString() {
        return AbiCodec.name(this);
    }

    /// All instances are equal: the type has no attributes.
    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj != null && obj.getClass() == getClass();
    }

    /// The same for all instances, consistent with [#equals(Object)].
    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }
}
