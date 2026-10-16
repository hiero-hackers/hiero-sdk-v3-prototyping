package org.hiero.keys.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.hiero.keys.Key;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeyType;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.keys.RawFormat;

/// The cryptography of the two key algorithms of Hiero: Ed25519 (RFC 8032) and ECDSA over secp256k1 with a
/// Keccak-256 pre-hash. Keys are plain values (`bytes`, `algorithm`, `type`); everything that depends on the algorithm
/// is dispatched here. The algorithm of an encoded key is taken from the ASN.1 object identifier of its structure,
/// never guessed from the length of the key material.
public final class Keys {

    private static final int ED25519_KEY_LENGTH = Ed25519.SECRET_KEY_SIZE;
    private static final int ECDSA_PRIVATE_KEY_LENGTH = 32;
    private static final int ECDSA_COMPRESSED_LENGTH = 33;
    private static final int ECDSA_UNCOMPRESSED_LENGTH = 65;
    private static final int SIGNATURE_LENGTH = 64;

    private static final SecureRandom RANDOM = new SecureRandom();

    private Keys() {
    }

    /// Creates a private key from its raw bytes: the 32-byte seed of Ed25519, the 32-byte scalar of ECDSA.
    ///
    /// @param algorithm the algorithm
    /// @param raw       the raw bytes
    /// @return the key
    /// @throws IllegalArgumentException if the bytes are no private key of that algorithm
    public static PrivateKey privateKey(final KeyAlgorithm algorithm, final byte[] raw) {
        switch (algorithm) {
            case ED25519 -> requireLength(raw, ED25519_KEY_LENGTH, "An Ed25519 private key");
            case ECDSA -> {
                requireLength(raw, ECDSA_PRIVATE_KEY_LENGTH, "An ECDSA secp256k1 private key");
                final BigInteger scalar = new BigInteger(1, raw);
                if (scalar.signum() == 0 || scalar.compareTo(Secp256k1.DOMAIN.getN()) >= 0) {
                    throw new IllegalArgumentException("An ECDSA secp256k1 private key must be in the range [1, n)");
                }
            }
        }
        return new PrivateKey(raw.clone(), algorithm, KeyType.PRIVATE);
    }

    /// Creates a public key from its raw bytes: the 32 bytes of Ed25519, the compressed or uncompressed point of
    /// ECDSA. An ECDSA key always holds the compressed point, so that equal keys are equal.
    ///
    /// @param algorithm the algorithm
    /// @param raw       the raw bytes
    /// @return the key
    /// @throws IllegalArgumentException if the bytes are no public key of that algorithm
    public static PublicKey publicKey(final KeyAlgorithm algorithm, final byte[] raw) {
        return switch (algorithm) {
            case ED25519 -> {
                requireLength(raw, ED25519_KEY_LENGTH, "An Ed25519 public key");
                yield new PublicKey(raw.clone(), algorithm, KeyType.PUBLIC);
            }
            case ECDSA -> new PublicKey(point(raw).getEncoded(true), algorithm, KeyType.PUBLIC);
        };
    }

    /// Generates a new private key.
    ///
    /// @param algorithm the algorithm
    /// @return the key
    public static PrivateKey generate(final KeyAlgorithm algorithm) {
        return switch (algorithm) {
            case ED25519 -> {
                final byte[] seed = new byte[ED25519_KEY_LENGTH];
                RANDOM.nextBytes(seed);
                yield privateKey(algorithm, seed);
            }
            case ECDSA -> {
                final BigInteger order = Secp256k1.DOMAIN.getN();
                BigInteger scalar;
                do {
                    scalar = new BigInteger(order.bitLength(), RANDOM);
                } while (scalar.signum() == 0 || scalar.compareTo(order) >= 0);
                yield privateKey(algorithm, Secp256k1.toFixedLength(scalar, ECDSA_PRIVATE_KEY_LENGTH));
            }
        };
    }

    /// Derives the public key of a private key.
    ///
    /// @param key the private key
    /// @return the public key
    public static PublicKey publicKey(final PrivateKey key) {
        requireValid(key);
        return switch (key.algorithm()) {
            case ED25519 -> {
                final byte[] publicKey = new byte[Ed25519.PUBLIC_KEY_SIZE];
                Ed25519.generatePublicKey(key.bytes(), 0, publicKey, 0);
                yield new PublicKey(publicKey, KeyAlgorithm.ED25519, KeyType.PUBLIC);
            }
            case ECDSA -> new PublicKey(Secp256k1.DOMAIN.getG().multiply(scalar(key)).normalize().getEncoded(true),
                    KeyAlgorithm.ECDSA, KeyType.PUBLIC);
        };
    }

    /// Signs a message: Ed25519 over the message (RFC 8032), ECDSA over its Keccak-256 hash with the deterministic nonce of
    /// RFC 6979 and `s` normalised to the lower half (low-s), as the 64 bytes `r || s`.
    ///
    /// @param key     the private key
    /// @param message the message
    /// @return the signature
    public static byte[] sign(final PrivateKey key, final byte[] message) {
        requireValid(key);
        return switch (key.algorithm()) {
            case ED25519 -> {
                final byte[] signature = new byte[Ed25519.SIGNATURE_SIZE];
                Ed25519.sign(key.bytes(), 0, message, 0, message.length, signature, 0);
                yield signature;
            }
            case ECDSA -> {
                final ECDSASigner signer = Secp256k1.deterministicSigner();
                signer.init(true, new ECPrivateKeyParameters(scalar(key), Secp256k1.DOMAIN));
                final BigInteger[] rs = signer.generateSignature(Secp256k1.keccak256(message));
                // low-s: of the two valid values s and n - s, the one in the lower half (the canonical form)
                final BigInteger order = Secp256k1.DOMAIN.getN();
                final BigInteger lowS = rs[1].compareTo(order.shiftRight(1)) > 0 ? order.subtract(rs[1]) : rs[1];
                final byte[] signature = new byte[SIGNATURE_LENGTH];
                System.arraycopy(Secp256k1.toFixedLength(rs[0], 32), 0, signature, 0, 32);
                System.arraycopy(Secp256k1.toFixedLength(lowS, 32), 0, signature, 32, 32);
                yield signature;
            }
        };
    }

    /// Verifies a signature made by [#sign(PrivateKey, byte[])]. An ECDSA signature with a high `s` is valid too.
    ///
    /// @param key       the public key
    /// @param message   the message
    /// @param signature the signature
    /// @return whether the signature is valid for the message
    public static boolean verify(final PublicKey key, final byte[] message, final byte[] signature) {
        if (signature.length != SIGNATURE_LENGTH) {
            return false;
        }
        return switch (key.algorithm()) {
            case ED25519 -> Ed25519.verify(signature, 0, key.bytes(), 0, message, 0, message.length);
            case ECDSA -> {
                final ECDSASigner signer = Secp256k1.deterministicSigner();
                signer.init(false, new ECPublicKeyParameters(point(key.bytes()), Secp256k1.DOMAIN));
                yield signer.verifySignature(Secp256k1.keccak256(message), new BigInteger(1, signature, 0, 32),
                        new BigInteger(1, signature, 32, 32));
            }
        };
    }

    /// Encodes a key in a binary format.
    ///
    /// @param key    the key
    /// @param format the format
    /// @return the encoded key
    /// @throws IllegalArgumentException if the format cannot hold the key or is a text format
    public static byte[] toBytes(final Key key, final KeyFormat format) {
        requireFormat(key, format, RawFormat.BYTES, "is text, use toString(KeyFormat)");
        try {
            return switch (key.type()) {
                case PRIVATE -> pkcs8((PrivateKey) key).getEncoded("DER");
                case PUBLIC -> new SubjectPublicKeyInfo(algorithmIdentifier(key.algorithm()), key.bytes())
                        .getEncoded("DER");
            };
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot encode the key as " + format, e);
        }
    }

    /// Encodes a key in a text format.
    ///
    /// @param key    the key
    /// @param format the format
    /// @return the encoded key
    /// @throws IllegalArgumentException if the format cannot hold the key or is a binary format
    public static String toString(final Key key, final KeyFormat format) {
        requireFormat(key, format, RawFormat.STRING, "is binary, use toBytes(KeyFormat)");
        final KeyFormat der = key.type() == KeyType.PRIVATE ? KeyFormat.PKCS8_WITH_DER : KeyFormat.SPKI_WITH_DER;
        return Pem.toPem(key.type(), toBytes(key, der));
    }

    /// Reads a private key from a PKCS#8 `PrivateKeyInfo`.
    ///
    /// @param der the DER-encoded structure
    /// @return the key
    /// @throws IllegalArgumentException if the structure is no PKCS#8 key of a supported algorithm
    public static PrivateKey fromPkcs8(final byte[] der) {
        final PrivateKeyInfo info;
        try {
            info = PrivateKeyInfo.getInstance(der);
        } catch (final RuntimeException notPkcs8) {
            throw new IllegalArgumentException("Not a PKCS#8 private key structure", notPkcs8);
        }
        if (info == null) {
            throw new IllegalArgumentException("Not a PKCS#8 private key structure");
        }
        final ASN1ObjectIdentifier algorithm = info.getPrivateKeyAlgorithm().getAlgorithm();
        try {
            if (Oids.ID_ED25519.equals(algorithm)) {
                return privateKey(KeyAlgorithm.ED25519,
                        ASN1OctetString.getInstance(info.parsePrivateKey()).getOctets());
            }
            if (Oids.ID_EC_PUBLIC_KEY.equals(algorithm)) {
                requireSecp256k1(info.getPrivateKeyAlgorithm().getParameters());
                final BigInteger scalar = ECPrivateKey.getInstance(info.parsePrivateKey()).getKey();
                return privateKey(KeyAlgorithm.ECDSA, Secp256k1.toFixedLength(scalar, ECDSA_PRIVATE_KEY_LENGTH));
            }
        } catch (final IOException | RuntimeException malformed) {
            throw new IllegalArgumentException("Malformed PKCS#8 private key", malformed);
        }
        throw new IllegalArgumentException("Unsupported PKCS#8 key algorithm: " + algorithm.getId());
    }

    /// Reads a public key from an SPKI `SubjectPublicKeyInfo`.
    ///
    /// @param der the DER-encoded structure
    /// @return the key
    /// @throws IllegalArgumentException if the structure is no SPKI key of a supported algorithm
    public static PublicKey fromSpki(final byte[] der) {
        final SubjectPublicKeyInfo info;
        try {
            info = SubjectPublicKeyInfo.getInstance(der);
        } catch (final RuntimeException notSpki) {
            throw new IllegalArgumentException("Not an SPKI public key structure", notSpki);
        }
        if (info == null) {
            throw new IllegalArgumentException("Not an SPKI public key structure");
        }
        final ASN1ObjectIdentifier algorithm = info.getAlgorithm().getAlgorithm();
        try {
            if (Oids.ID_ED25519.equals(algorithm)) {
                return publicKey(KeyAlgorithm.ED25519, info.getPublicKeyData().getBytes());
            }
            if (Oids.ID_EC_PUBLIC_KEY.equals(algorithm)) {
                requireSecp256k1(info.getAlgorithm().getParameters());
                return publicKey(KeyAlgorithm.ECDSA, info.getPublicKeyData().getBytes());
            }
        } catch (final RuntimeException malformed) {
            throw new IllegalArgumentException("Malformed SPKI public key", malformed);
        }
        throw new IllegalArgumentException("Unsupported SPKI key algorithm: " + algorithm.getId());
    }

    /// Checks that a format can hold a key type in the given raw form.
    ///
    /// @param format the format
    /// @param type   the key type
    /// @param raw    the raw form the caller has (bytes or text)
    /// @throws IllegalArgumentException if it cannot
    public static void requireFormat(final KeyFormat format, final KeyType type, final RawFormat raw) {
        if (!format.supportsType(type)) {
            throw new IllegalArgumentException("Format " + format + " cannot hold a " + type + " key");
        }
        if (format.encoding().rawFormat() != raw) {
            throw new IllegalArgumentException("Format " + format + " is " + (raw == RawFormat.BYTES ? "text"
                    : "binary") + ", it cannot be read from " + (raw == RawFormat.BYTES ? "bytes" : "text"));
        }
    }

    private static PrivateKeyInfo pkcs8(final PrivateKey key) throws IOException {
        return switch (key.algorithm()) {
            case ED25519 -> new PrivateKeyInfo(algorithmIdentifier(KeyAlgorithm.ED25519),
                    new DEROctetString(key.bytes()));
            case ECDSA -> new PrivateKeyInfo(algorithmIdentifier(KeyAlgorithm.ECDSA),
                    new ECPrivateKey(256, scalar(key), new DERBitString(publicKey(key).bytes()),
                            new X962Parameters(Oids.ID_SECP256K1)));
        };
    }

    private static AlgorithmIdentifier algorithmIdentifier(final KeyAlgorithm algorithm) {
        return switch (algorithm) {
            case ED25519 -> new AlgorithmIdentifier(Oids.ID_ED25519);
            case ECDSA -> new AlgorithmIdentifier(Oids.ID_EC_PUBLIC_KEY, Oids.ID_SECP256K1);
        };
    }

    private static void requireFormat(final Key key, final KeyFormat format, final RawFormat raw,
                                      final String otherwise) {
        if (!format.supportsType(key.type())) {
            throw new IllegalArgumentException("Format " + format + " cannot hold a " + key.type() + " key");
        }
        if (format.encoding().rawFormat() != raw) {
            throw new IllegalArgumentException("Format " + format + " " + otherwise);
        }
    }

    private static void requireSecp256k1(final ASN1Encodable parameters) {
        if (!Oids.ID_SECP256K1.equals(parameters)) {
            throw new IllegalArgumentException("Unsupported elliptic curve, Hiero keys use secp256k1");
        }
    }

    private static void requireLength(final byte[] raw, final int length, final String what) {
        if (raw.length != length) {
            throw new IllegalArgumentException(what + " has " + length + " bytes, not " + raw.length);
        }
    }

    /// `PrivateKey` has a public constructor that takes any bytes, so a key is checked before it is used.
    private static void requireValid(final PrivateKey key) {
        privateKey(key.algorithm(), key.bytes());
    }

    private static BigInteger scalar(final PrivateKey key) {
        return new BigInteger(1, key.bytes());
    }

    private static ECPoint point(final byte[] encoded) {
        if (encoded.length != ECDSA_COMPRESSED_LENGTH && encoded.length != ECDSA_UNCOMPRESSED_LENGTH) {
            throw new IllegalArgumentException("An ECDSA secp256k1 public key has " + ECDSA_COMPRESSED_LENGTH + " or "
                    + ECDSA_UNCOMPRESSED_LENGTH + " bytes, not " + encoded.length);
        }
        try {
            return Secp256k1.DOMAIN.getCurve().decodePoint(encoded).normalize();
        } catch (final IllegalArgumentException invalid) {
            throw new IllegalArgumentException("The ECDSA secp256k1 public key is not a point on the curve", invalid);
        }
    }
}
