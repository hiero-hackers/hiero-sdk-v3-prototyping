// Generated from spec/base/ledger.md (ledger#EvmAddress); DO NOT EDIT.

package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.Objects;

/** Immutable fixed-length network-order byte value. */
public final class EvmAddress {
    private final byte[] bytes;
    /** Creates a value and defensively copies the bytes. */
    public EvmAddress(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length != 20) throw new IllegalArgumentException("bytes must contain exactly 20 elements");
        this.bytes = bytes.clone();
    }
    /** Returns a defensive copy. */ public byte[] bytes() { return bytes.clone(); }
    @Override public boolean equals(Object other) { return other instanceof EvmAddress value && Arrays.equals(bytes, value.bytes); }
    @Override public int hashCode() { return Arrays.hashCode(bytes); }
}
