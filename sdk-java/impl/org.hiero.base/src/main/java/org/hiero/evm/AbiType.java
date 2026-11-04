package org.hiero.evm;

import org.hiero.evm.internal.AbiSignatures;


/// The type of an ABI value. Two types are equal if they have the same canonical name.
public sealed interface AbiType permits AbiBoolType, AbiAddressType, AbiUintType, AbiIntType, AbiFixedBytesType, AbiBytesType, AbiStringType, AbiArrayType, AbiTupleType {

    /// The canonical name of the type, as used in a signature: "uint256", "bytes32", "(address,uint256)[]".
    @Override
    String toString();

    /// Reads a type from its name. "uint" and "int" are read as "uint256" and "int256"; spaces are not allowed. Throws
    /// for a name that is no supported ABI type.
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    static AbiType parse(final String text) {
        return AbiSignatures.parseType(text);
    }
}
