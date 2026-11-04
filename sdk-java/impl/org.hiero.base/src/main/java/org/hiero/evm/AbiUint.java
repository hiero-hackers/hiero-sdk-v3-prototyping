package org.hiero.evm;

import java.math.BigInteger;
import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;

/// A `uint<bits>`. The value must be below 2^bits; this is checked when the value is encoded.
///
/// @param bits the width of the type
public record AbiUint(BigInteger value, int bits) implements AbiValue {

    private static final BigInteger VALUE_MIN = new BigInteger("0");
    private static final BigInteger VALUE_MAX = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935");

    /// Creates a new `AbiUint`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AbiUint {
        Objects.requireNonNull(value, "value must not be null");
        if (value.compareTo(VALUE_MIN) < 0 || value.compareTo(VALUE_MAX) > 0) {
            throw new IllegalArgumentException("value must be between 0 and 115792089237316195423570985008687907853269984665640564039457584007913129639935");
        }
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

    /// Creates a new `AbiUint` with the default value of `bits`.
    public AbiUint(final BigInteger value) {
        this(value, 256);
    }

    /// The type of the value.
    @Override
    public AbiType abiType() {
        return AbiCodec.typeOf(this);
    }
}
