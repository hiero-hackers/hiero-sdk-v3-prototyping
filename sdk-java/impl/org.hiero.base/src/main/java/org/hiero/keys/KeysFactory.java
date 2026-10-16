package org.hiero.keys;

import java.util.Objects;
import org.hiero.keys.internal.Keys;

/// Factory methods of the package `org.hiero.keys`.
public final class KeysFactory {

    /// Generate a new key based on a specific algorithm
    public static PrivateKey generatePrivateKey(final KeyAlgorithm algorithm) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        return Keys.generate(algorithm);
    }

    public static PublicKey generatePublicKey(final KeyAlgorithm algorithm) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        return Keys.publicKey(Keys.generate(algorithm));
    }

    /// Read a key based on a specific algorithm from a string
    /// calls createPrivateKey(algorithm: KeyAlgorithm, rawBytes: bytes)
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PrivateKey createPrivateKey(final KeyAlgorithm algorithm, final ByteImportEncoding encoding, final String value) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        Objects.requireNonNull(encoding, "encoding must not be null");
        Objects.requireNonNull(value, "value must not be null");
        return Keys.privateKey(algorithm, encoding.decode(value));
    }

    /// calls createPublicKey(algorithm: KeyAlgorithm, rawBytes: bytes)
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PublicKey createPublicKey(final KeyAlgorithm algorithm, final ByteImportEncoding encoding, final String value) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        Objects.requireNonNull(encoding, "encoding must not be null");
        Objects.requireNonNull(value, "value must not be null");
        return Keys.publicKey(algorithm, encoding.decode(value));
    }

    /// Read a key based on a specific algorithm from a byte array
    /// reads bytes as raw bytes for the given algorithm
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PrivateKey createPrivateKey(final KeyAlgorithm algorithm, final byte[] rawBytes) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        Objects.requireNonNull(rawBytes, "rawBytes must not be null");
        return Keys.privateKey(algorithm, rawBytes);
    }

    /// reads bytes as raw bytes for the given algorithm
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PublicKey createPublicKey(final KeyAlgorithm algorithm, final byte[] rawBytes) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        Objects.requireNonNull(rawBytes, "rawBytes must not be null");
        return Keys.publicKey(algorithm, rawBytes);
    }

    /// Read a key based on a specific format (container & encoding) from a string
    /// if container.format is not STRING an illegal format error is thrown
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PrivateKey createPrivateKey(final KeyFormat container, final String value) {
        Objects.requireNonNull(container, "container must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Keys.requireFormat(container, KeyType.PRIVATE, RawFormat.STRING);
        return Keys.fromPkcs8(container.decode(KeyType.PRIVATE, value));
    }

    /// if container.format is not STRING an illegal format error is thrown
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PublicKey createPublicKey(final KeyFormat container, final String value) {
        Objects.requireNonNull(container, "container must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Keys.requireFormat(container, KeyType.PUBLIC, RawFormat.STRING);
        return Keys.fromSpki(container.decode(KeyType.PUBLIC, value));
    }

    /// Read a key based on a specific format (container & encoding) from a byte array
    /// if container.format is not BYTES an illegal format error is thrown
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PrivateKey createPrivateKey(final KeyFormat container, final byte[] value) {
        Objects.requireNonNull(container, "container must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Keys.requireFormat(container, KeyType.PRIVATE, RawFormat.BYTES);
        return Keys.fromPkcs8(value);
    }

    /// if container.format is not BYTES an illegal format error is thrown
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PublicKey createPublicKey(final KeyFormat container, final byte[] value) {
        Objects.requireNonNull(container, "container must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Keys.requireFormat(container, KeyType.PUBLIC, RawFormat.BYTES);
        return Keys.fromSpki(value);
    }

    /// Read a key based on our preferred format (container & encoding) from a string
    /// reads string as PKCS#8 PEM
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PrivateKey createPrivateKey(final String value) {
        Objects.requireNonNull(value, "value must not be null");
        return createPrivateKey(KeyFormat.PKCS8_WITH_PEM, value);
    }

    /// reads string as SPKI PEM
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public static PublicKey createPublicKey(final String value) {
        Objects.requireNonNull(value, "value must not be null");
        return createPublicKey(KeyFormat.SPKI_WITH_PEM, value);
    }

    private KeysFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}
