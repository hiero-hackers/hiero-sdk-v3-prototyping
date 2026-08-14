// Source: spec/base/ledger.md (ledger#IpAddress).

package org.hiero.sdk.v3.ledger;

import java.util.Arrays;
import java.util.Objects;

/** Immutable four-byte IPv4 address value. */
public final class IpAddress {
    private final byte[] bytes;

    /** Creates a value and defensively owns exactly four network-order bytes. */
    public IpAddress(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length != 4) throw new IllegalArgumentException("bytes must contain exactly 4 elements");
        this.bytes = bytes.clone();
    }

    /** Wraps validated raw bytes without parsing textual input. */
    public static IpAddress fromBytes(byte[] value) { return new IpAddress(value); }

    /** Returns a defensive copy. */
    public byte[] bytes() { return bytes.clone(); }

    /** Returns dotted-quad IPv4 form. */
    @Override
    public String toString() {
        return Byte.toUnsignedInt(bytes[0]) + "." + Byte.toUnsignedInt(bytes[1]) + "."
                + Byte.toUnsignedInt(bytes[2]) + "." + Byte.toUnsignedInt(bytes[3]);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IpAddress value && Arrays.equals(bytes, value.bytes);
    }

    @Override
    public int hashCode() { return Arrays.hashCode(bytes); }
}
