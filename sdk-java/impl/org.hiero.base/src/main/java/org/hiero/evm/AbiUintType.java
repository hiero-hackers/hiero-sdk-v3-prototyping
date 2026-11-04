package org.hiero.evm;

import org.hiero.evm.internal.AbiCodec;


/// The type `uint<bits>`: an unsigned integer of the given width.
///
/// @param bits the width in bits
public record AbiUintType(int bits) implements AbiType {

    /// Creates a new `AbiUintType`.
    ///
    /// @throws IllegalArgumentException if a value violates its constraints
    public AbiUintType {
        if (bits < 0 || bits > 65535) {
            throw new IllegalArgumentException("bits must be between 0 and 65535");
        }
        if (bits < 8) {
            throw new IllegalArgumentException("bits must be at least 8");
        }
        if (bits > 256) {
            throw new IllegalArgumentException("bits must be at most 256");
        }
        if (bits % 8 != 0) {
            throw new IllegalArgumentException("bits must be a multiple of 8");
        }
    }

    /// Creates a new `AbiUintType` with the default value of `bits`.
    public AbiUintType() {
        this(256);
    }

    /// The canonical name of the type, as used in a signature: "uint256", "bytes32", "(address,uint256)[]".
    @Override
    public String toString() {
        return AbiCodec.name(this);
    }
}
