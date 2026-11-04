package org.hiero.evm;

import java.math.BigInteger;
import java.util.Objects;
import org.hiero.evm.internal.AbiCodec;

/// An `int<bits>`. The value must be between -2^(bits-1) and 2^(bits-1)-1; this is checked when the value is encoded.
///
/// @param bits the width of the type
public record AbiInt(BigInteger value, int bits) implements AbiValue {

    private static final BigInteger VALUE_MIN = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
    private static final BigInteger VALUE_MAX = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");

    /// Creates a new `AbiInt`.
    ///
    /// @throws NullPointerException if a required value is `null`
    /// @throws IllegalArgumentException if a value violates its constraints
    public AbiInt {
        Objects.requireNonNull(value, "value must not be null");
        if (value.compareTo(VALUE_MIN) < 0 || value.compareTo(VALUE_MAX) > 0) {
            throw new IllegalArgumentException("value must be between -57896044618658097711785492504343953926634992332820282019728792003956564819968 and 57896044618658097711785492504343953926634992332820282019728792003956564819967");
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

    /// Creates a new `AbiInt` with the default value of `bits`.
    public AbiInt(final BigInteger value) {
        this(value, 256);
    }

    /// The type of the value.
    @Override
    public AbiType abiType() {
        return AbiCodec.typeOf(this);
    }
}
