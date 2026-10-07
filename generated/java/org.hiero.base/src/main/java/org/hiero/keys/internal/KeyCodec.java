package org.hiero.keys.internal;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;

/// Reads the DER structures of the key formats. The algorithm is taken from the ASN.1 object
/// identifier of the structure, never guessed from the length of the key material.
public final class KeyCodec {

    private KeyCodec() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
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
                final ASN1Encodable parsed = info.parsePrivateKey();
                return new Ed25519PrivateKey(ASN1OctetString.getInstance(parsed).getOctets());
            }
            if (Oids.ID_EC_PUBLIC_KEY.equals(algorithm)) {
                requireSecp256k1(info.getPrivateKeyAlgorithm().getParameters());
                final ECPrivateKey sec1 = ECPrivateKey.getInstance(info.parsePrivateKey());
                return new EcdsaPrivateKey(sec1.getKey());
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
                return new Ed25519PublicKey(info.getPublicKeyData().getBytes());
            }
            if (Oids.ID_EC_PUBLIC_KEY.equals(algorithm)) {
                requireSecp256k1(info.getAlgorithm().getParameters());
                return new EcdsaPublicKey(info.getPublicKeyData().getBytes());
            }
        } catch (final RuntimeException malformed) {
            throw new IllegalArgumentException("Malformed SPKI public key", malformed);
        }
        throw new IllegalArgumentException("Unsupported SPKI key algorithm: " + algorithm.getId());
    }

    private static void requireSecp256k1(final ASN1Encodable parameters) {
        if (!Oids.ID_SECP256K1.equals(parameters)) {
            throw new IllegalArgumentException("Unsupported elliptic curve, Hiero keys use secp256k1");
        }
    }
}
