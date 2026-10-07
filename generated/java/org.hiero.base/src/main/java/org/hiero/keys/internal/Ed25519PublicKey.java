package org.hiero.keys.internal;

import java.io.IOException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeyType;
import org.hiero.keys.PublicKey;
import org.hiero.keys.RawFormat;

/// An Ed25519 public key, stored as its 32 raw bytes.
public final class Ed25519PublicKey extends PublicKey {

    /// Creates a key from its raw bytes.
    ///
    /// @param publicKey the 32 raw bytes
    /// @throws IllegalArgumentException if the key does not have 32 bytes
    public Ed25519PublicKey(final byte[] publicKey) {
        super(checkedKey(publicKey), KeyAlgorithm.ED25519, KeyType.PUBLIC);
    }

    private static byte[] checkedKey(final byte[] publicKey) {
        if (publicKey.length != Ed25519.PUBLIC_KEY_SIZE) {
            throw new IllegalArgumentException(
                    "An Ed25519 public key has " + Ed25519.PUBLIC_KEY_SIZE + " bytes, not " + publicKey.length);
        }
        return publicKey;
    }

    @Override
    public byte[] toRawBytes() {
        return bytes();
    }

    @Override
    public boolean verify(final byte[] message, final byte[] signature) {
        if (signature.length != Ed25519.SIGNATURE_SIZE) {
            return false;
        }
        return Ed25519.verify(signature, 0, bytes(), 0, message, 0, message.length);
    }

    @Override
    public byte[] toBytes(final KeyFormat container) {
        if (!container.supportsType(KeyType.PUBLIC)) {
            throw new IllegalArgumentException("Format " + container + " does not hold a public key");
        }
        if (container.encoding().rawFormat() != RawFormat.BYTES) {
            throw new IllegalArgumentException("Format " + container + " is text, use toString(KeyFormat)");
        }
        try {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(Oids.ID_ED25519), bytes()).getEncoded("DER");
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot encode the Ed25519 public key as " + container, e);
        }
    }

    @Override
    public String toString(final KeyFormat container) {
        if (!container.supportsType(KeyType.PUBLIC)) {
            throw new IllegalArgumentException("Format " + container + " does not hold a public key");
        }
        if (container.encoding().rawFormat() != RawFormat.STRING) {
            throw new IllegalArgumentException("Format " + container + " is binary, use toBytes(KeyFormat)");
        }
        return Pem.toPem(KeyType.PUBLIC, toBytes(KeyFormat.SPKI_WITH_DER));
    }
}
