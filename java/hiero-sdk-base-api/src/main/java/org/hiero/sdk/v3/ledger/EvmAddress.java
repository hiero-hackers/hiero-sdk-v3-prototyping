// Source: spec/base/ledger.md (ledger#EvmAddress).

package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable 20-byte EVM-address base contract.
 * Implementations supply the cryptographic EIP-55 representation.
 */
public abstract class EvmAddress {
    private final byte[] bytes;

    /** Creates an address and defensively owns exactly twenty bytes. */
    protected EvmAddress(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length != 20) throw new IllegalArgumentException("bytes must contain exactly 20 elements");
        this.bytes = bytes.clone();
    }

    /** Returns a defensive copy of the network-order bytes. */
    public final byte[] bytes() { return bytes.clone(); }

    /** Returns the implementation-provided EIP-55 form. */
    @Override
    public abstract String toString();

    @Override
    public final boolean equals(Object other) {
        return other instanceof EvmAddress value && Arrays.equals(bytes, value.bytes);
    }

    @Override
    public final int hashCode() { return Arrays.hashCode(bytes); }
}
