package org.hiero.keys;


/// all supported container formats
public enum KeyContainer {

    /// PKCS#8 private key (RFC 5958); an ECDSA key as id-ecPublicKey with the curve secp256k1 (RFC 5480, RFC 5915)
    PKCS8,
    /// SubjectPublicKeyInfo (RFC 5280); an ECDSA key as id-ecPublicKey with the curve secp256k1 and the compressed point (RFC 5480)
    SPKI;

    /// returns true if the container format supports the given key type
    public boolean supportsType(final KeyType type) {
        return switch (this) {
            case PKCS8 -> type == KeyType.PRIVATE;
            case SPKI -> type == KeyType.PUBLIC;
        };
    }
}
