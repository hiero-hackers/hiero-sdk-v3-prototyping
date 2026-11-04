package org.hiero.evm;


/// A value of an ABI type. Two values are equal if they have the same type and the same content.
public sealed interface AbiValue permits AbiBool, AbiAddress, AbiUint, AbiInt, AbiFixedBytes, AbiBytes, AbiString, AbiArray, AbiTuple {

    /// The type of the value.
    AbiType abiType();
}
