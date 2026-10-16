package org.hiero.keys;

import java.util.Arrays;
import java.util.Objects;
import org.hiero.keys.internal.Keys;
import org.jspecify.annotations.Nullable;

/// private key definition
public class PrivateKey extends Key {

    /// Creates a new `PrivateKey`.
    public PrivateKey(final byte[] bytes, final KeyAlgorithm algorithm, final KeyType type) {
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

    /// Sign a message with this private key
    /// returns the signature for the message
    public byte[] sign(final byte[] message) {
        return Keys.sign(this, message);
    }

    /// Derive the corresponding public key
    /// always returns a new PublicKey instance
    public PublicKey createPublicKey() {
        return Keys.publicKey(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof PrivateKey other
            && Arrays.equals(bytes(), other.bytes())
            && Objects.equals(algorithm(), other.algorithm())
            && Objects.equals(type(), other.type());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(bytes()), algorithm(), type());
    }
}
