package org.hiero.keys.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeyType;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.keys.RawFormat;

/// An ECDSA secp256k1 private key, stored as the 32 big-endian bytes of its scalar.
public final class EcdsaPrivateKey extends PrivateKey {

    private static final int SCALAR_LENGTH = 32;

    private final BigInteger scalar;

    /// Creates a key from the big-endian bytes of its scalar.
    ///
    /// @param scalarBytes the 32 bytes of the scalar
    /// @throws IllegalArgumentException if the value does not have 32 bytes or is not a valid scalar
    public EcdsaPrivateKey(final byte[] scalarBytes) {
        super(checkedScalar(scalarBytes), KeyAlgorithm.ECDSA, KeyType.PRIVATE);
        this.scalar = new BigInteger(1, scalarBytes);
    }

    /// Creates a key from its scalar.
    ///
    /// @param scalar the scalar
    /// @throws IllegalArgumentException if the scalar is not in `[1, n)`
    public EcdsaPrivateKey(final BigInteger scalar) {
        this(Secp256k1.toFixedLength(scalar, SCALAR_LENGTH));
    }

    /// Creates a key from a new random scalar.
    ///
    /// @return the key
    public static EcdsaPrivateKey generate() {
        final SecureRandom random = new SecureRandom();
        final BigInteger order = Secp256k1.DOMAIN.getN();
        BigInteger candidate;
        do {
            candidate = new BigInteger(order.bitLength(), random);
        } while (candidate.signum() <= 0 || candidate.compareTo(order) >= 0);
        return new EcdsaPrivateKey(candidate);
    }

    private static byte[] checkedScalar(final byte[] scalarBytes) {
        if (scalarBytes.length != SCALAR_LENGTH) {
            throw new IllegalArgumentException(
                    "An ECDSA secp256k1 private key has " + SCALAR_LENGTH + " bytes, not " + scalarBytes.length);
        }
        final BigInteger value = new BigInteger(1, scalarBytes);
        if (value.signum() <= 0 || value.compareTo(Secp256k1.DOMAIN.getN()) >= 0) {
            throw new IllegalArgumentException("The ECDSA secp256k1 private key is not in the range [1, n)");
        }
        return scalarBytes;
    }

    @Override
    public byte[] toRawBytes() {
        return bytes();
    }

    @Override
    public byte[] sign(final byte[] message) {
        final ECDSASigner signer = Secp256k1.deterministicSigner();
        signer.init(true, new ECPrivateKeyParameters(scalar, Secp256k1.DOMAIN));
        final BigInteger[] signature = signer.generateSignature(Secp256k1.keccak256(message));
        final byte[] encoded = new byte[64];
        System.arraycopy(Secp256k1.toFixedLength(signature[0], 32), 0, encoded, 0, 32);
        System.arraycopy(Secp256k1.toFixedLength(signature[1], 32), 0, encoded, 32, 32);
        return encoded;
    }

    @Override
    public PublicKey createPublicKey() {
        final ECPoint point = Secp256k1.DOMAIN.getG().multiply(scalar).normalize();
        return new EcdsaPublicKey(point.getEncoded(true));
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
            final ECPoint point = Secp256k1.DOMAIN.getG().multiply(scalar).normalize();
            final ECPrivateKey sec1 = new ECPrivateKey(256, scalar, new DERBitString(point.getEncoded(true)),
                    new X962Parameters(Oids.ID_SECP256K1));
            final AlgorithmIdentifier algorithm =
                    new AlgorithmIdentifier(Oids.ID_EC_PUBLIC_KEY, Oids.ID_SECP256K1);
            return new PrivateKeyInfo(algorithm, sec1).getEncoded("DER");
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot encode the ECDSA private key as " + container, e);
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
