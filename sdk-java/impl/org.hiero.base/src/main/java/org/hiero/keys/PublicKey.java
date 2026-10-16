package org.hiero.keys;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.keys.internal.Keys;
import org.jspecify.annotations.Nullable;

/// public key definition
public class PublicKey extends Key {

    /// Creates a new `PublicKey`.
    public PublicKey(final byte[] bytes, final KeyAlgorithm algorithm, final KeyType type) {
        super(bytes, algorithm, type);
    }

    /// returns the key in the RAW encoding
    @Override
    public byte[] toRawBytes() {
        return bytes();
    }

    /// Convert to bytes using specified container format
    /// Throws illegal-format if container.format is not BYTES or doesn't support this key type
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    @Override
    public byte[] toBytes(final KeyFormat container) {
        return Keys.toBytes(this, container);
    }

    /// Convert to string using specified container format
    /// Throws illegal-format if container.format is not STRING or doesn't support this key type
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    @Override
    public String toString(final KeyFormat container) {
        return Keys.toString(this, container);
    }

    /// Verify a signature using this public key
    /// returns true if the signature is valid for the message and the public key
    public boolean verify(final byte[] message, final byte[] signature) {
        return Keys.verify(this, message, signature);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof PublicKey other
            && Arrays.equals(bytes(), other.bytes())
            && Objects.equals(algorithm(), other.algorithm())
            && Objects.equals(type(), other.type());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(bytes()), algorithm(), type());
    }
}
