package org.hiero.keys.internal;

import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeyType;
import org.hiero.keys.PublicKey;
import org.hiero.keys.RawFormat;

/// An ECDSA secp256k1 public key, stored as its 33 compressed bytes.
public final class EcdsaPublicKey extends PublicKey {

    private static final int COMPRESSED_LENGTH = 33;
    private static final int UNCOMPRESSED_LENGTH = 65;
    private static final int SIGNATURE_LENGTH = 64;

    /// Creates a key from its encoded point. An uncompressed point is stored compressed.
    ///
    /// @param point the 33 compressed or 65 uncompressed bytes
    /// @throws IllegalArgumentException if the point has another length or is not on the curve
    public EcdsaPublicKey(final byte[] point) {
        super(compress(point), KeyAlgorithm.ECDSA, KeyType.PUBLIC);
    }

    private static byte[] compress(final byte[] point) {
        if (point.length != COMPRESSED_LENGTH && point.length != UNCOMPRESSED_LENGTH) {
            throw new IllegalArgumentException("An ECDSA secp256k1 public key has " + COMPRESSED_LENGTH + " or "
                    + UNCOMPRESSED_LENGTH + " bytes, not " + point.length);
        }
        try {
            return Secp256k1.DOMAIN.getCurve().decodePoint(point).normalize().getEncoded(true);
        } catch (final IllegalArgumentException invalid) {
            throw new IllegalArgumentException("The ECDSA secp256k1 public key is not a point on the curve", invalid);
        }
    }

    @Override
    public byte[] toRawBytes() {
        return bytes();
    }

    @Override
    public boolean verify(final byte[] message, final byte[] signature) {
        if (signature.length != SIGNATURE_LENGTH) {
            return false;
        }
        final BigInteger r = new BigInteger(1, signature, 0, 32);
        final BigInteger s = new BigInteger(1, signature, 32, 32);
        final ECPoint point = Secp256k1.DOMAIN.getCurve().decodePoint(bytes());
        final ECDSASigner signer = Secp256k1.deterministicSigner();
        signer.init(false, new ECPublicKeyParameters(point, Secp256k1.DOMAIN));
        return signer.verifySignature(Secp256k1.keccak256(message), r, s);
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
            final AlgorithmIdentifier algorithm =
                    new AlgorithmIdentifier(Oids.ID_EC_PUBLIC_KEY, Oids.ID_SECP256K1);
            return new SubjectPublicKeyInfo(algorithm, bytes()).getEncoded("DER");
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot encode the ECDSA public key as " + container, e);
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
