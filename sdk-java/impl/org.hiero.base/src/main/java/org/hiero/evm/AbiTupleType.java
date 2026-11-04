package org.hiero.evm;

import java.util.List;
import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;

/// A tuple type `(T1,…,Tn)`: a struct in Solidity, or the arguments or results of a function.
///
/// @param components the types of the components, in their order
public record AbiTupleType(List<AbiType> components) implements AbiType {

    /// Creates a new `AbiTupleType`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public AbiTupleType {
        Objects.requireNonNull(components, "components must not be null");
        components = List.copyOf(components);
    }

    /// The canonical name of the type, as used in a signature: "uint256", "bytes32", "(address,uint256)[]".
    @Override
    public String toString() {
        return AbiCodec.name(this);
    }

    /// Decodes a value of this type. Throws if the data is no valid encoding of it (see the description).
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public AbiTuple decode(final byte[] data) {
        return new AbiTuple(AbiCodec.decode(components, data));
    }
}
