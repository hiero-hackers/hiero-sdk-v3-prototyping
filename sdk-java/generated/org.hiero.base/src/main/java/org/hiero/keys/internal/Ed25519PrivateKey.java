package org.hiero.keys.internal;

import java.io.IOException;
import java.security.SecureRandom;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeyType;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.keys.RawFormat;

/// An Ed25519 private key, stored as its 32-byte seed.
public final class Ed25519PrivateKey extends PrivateKey {

    /// Creates a key from its seed.
    ///
    /// @param seed the 32-byte seed
    /// @throws IllegalArgumentException if the seed does not have 32 bytes
    public Ed25519PrivateKey(final byte[] seed) {
        super(checkedSeed(seed), KeyAlgorithm.ED25519, KeyType.PRIVATE);
    }

    /// Creates a key from a new random seed.
    ///
    /// @return the key
    public static Ed25519PrivateKey generate() {
        final byte[] seed = new byte[Ed25519.SECRET_KEY_SIZE];
        new SecureRandom().nextBytes(seed);
        return new Ed25519PrivateKey(seed);
    }

    private static byte[] checkedSeed(final byte[] seed) {
        if (seed.length != Ed25519.SECRET_KEY_SIZE) {
            throw new IllegalArgumentException(
                    "An Ed25519 private key has " + Ed25519.SECRET_KEY_SIZE + " bytes, not " + seed.length);
        }
        return seed;
    }

    @Override
    public byte[] toRawBytes() {
        return bytes();
    }

    @Override
    public byte[] sign(final byte[] message) {
        final byte[] seed = bytes();
        final byte[] signature = new byte[Ed25519.SIGNATURE_SIZE];
        Ed25519.sign(seed, 0, message, 0, message.length, signature, 0);
        return signature;
    }

    @Override
    public PublicKey createPublicKey() {
        final byte[] seed = bytes();
        final byte[] publicKey = new byte[Ed25519.PUBLIC_KEY_SIZE];
        Ed25519.generatePublicKey(seed, 0, publicKey, 0);
        return new Ed25519PublicKey(publicKey);
    }

    @Override
    public byte[] toBytes(final KeyFormat container) {
        if (!container.supportsType(KeyType.PRIVATE)) {
            throw new IllegalArgumentException("Format " + container + " does not hold a private key");
        }
        if (container.encoding().rawFormat() != RawFormat.BYTES) {
            throw new IllegalArgumentException("Format " + container + " is text, use toString(KeyFormat)");
        }
        try {
            return new PrivateKeyInfo(new AlgorithmIdentifier(Oids.ID_ED25519), new DEROctetString(bytes()))
                    .getEncoded("DER");
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot encode the Ed25519 private key as " + container, e);
        }
    }

    @Override
    public String toString(final KeyFormat container) {
        if (!container.supportsType(KeyType.PRIVATE)) {
            throw new IllegalArgumentException("Format " + container + " does not hold a private key");
        }
        if (container.encoding().rawFormat() != RawFormat.STRING) {
            throw new IllegalArgumentException("Format " + container + " is binary, use toBytes(KeyFormat)");
        }
        return Pem.toPem(KeyType.PRIVATE, toBytes(KeyFormat.PKCS8_WITH_DER));
    }
}
