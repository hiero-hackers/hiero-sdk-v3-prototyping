package org.hiero.evm;

import org.hiero.evm.internal.AbiCodec;


/// The type `bytes<size>`: a fixed number of bytes.
///
/// @param size the number of bytes
public record AbiFixedBytesType(short size) implements AbiType {

    /// Creates a new `AbiFixedBytesType`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public AbiFixedBytesType {
        if (size < (short) 0 || size > (short) 255) {
            throw new IllegalArgumentException("size must be between 0 and 255");
        }
        if (size < (short) 1) {
            throw new IllegalArgumentException("size must be at least 1");
        }
        if (size > (short) 32) {
            throw new IllegalArgumentException("size must be at most 32");
        }
    }

    /// The canonical name of the type, as used in a signature: "uint256", "bytes32", "(address,uint256)[]".
    @Override
    public String toString() {
        return AbiCodec.name(this);
    }
}
