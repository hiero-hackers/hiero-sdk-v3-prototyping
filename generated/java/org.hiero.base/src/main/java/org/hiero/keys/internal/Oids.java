package org.hiero.keys.internal;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/// The ASN.1 object identifiers the key encodings are dispatched on.
public final class Oids {

    /// `id-Ed25519` (RFC 8410), the algorithm of an Ed25519 PKCS#8 or SPKI structure.
    public static final ASN1ObjectIdentifier ID_ED25519 = new ASN1ObjectIdentifier("1.3.101.112");

    /// `id-ecPublicKey` (RFC 5480), the algorithm of every elliptic-curve key.
    public static final ASN1ObjectIdentifier ID_EC_PUBLIC_KEY = new ASN1ObjectIdentifier("1.2.840.10045.2.1");

    /// `secp256k1` (SEC 2), the only curve Hiero uses; the parameter of `id-ecPublicKey`.
    public static final ASN1ObjectIdentifier ID_SECP256K1 = new ASN1ObjectIdentifier("1.3.132.0.10");

    private Oids() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
