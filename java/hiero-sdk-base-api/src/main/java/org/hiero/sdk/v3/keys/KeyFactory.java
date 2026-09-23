// Source: spec/base/keys.md (keys#KeyFactory).

package org.hiero.sdk.v3.keys;

/** Body-free key generation and import contract. */
public interface KeyFactory {
    /** Generates a private key for the requested algorithm. */
    PrivateKey generatePrivateKey(KeyAlgorithm algorithm);
    /** Generates a public key for the requested algorithm. */
    PublicKey generatePublicKey(KeyAlgorithm algorithm);
    /** Imports a private key from encoded text with an explicit algorithm. */
    PrivateKey createPrivateKey(KeyAlgorithm algorithm, ByteImportEncoding encoding, String value);
    /** Imports a public key from encoded text with an explicit algorithm. */
    PublicKey createPublicKey(KeyAlgorithm algorithm, ByteImportEncoding encoding, String value);
    /** Imports a private key from raw bytes with an explicit algorithm. */
    PrivateKey createPrivateKey(KeyAlgorithm algorithm, byte[] rawBytes);
    /** Imports a public key from raw bytes with an explicit algorithm. */
    PublicKey createPublicKey(KeyAlgorithm algorithm, byte[] rawBytes);
    /** Imports a private key from container-formatted text. */
    PrivateKey createPrivateKey(KeyFormat container, String value);
    /** Imports a public key from container-formatted text. */
    PublicKey createPublicKey(KeyFormat container, String value);
    /** Imports a private key from container-formatted bytes. */
    PrivateKey createPrivateKey(KeyFormat container, byte[] value);
    /** Imports a public key from container-formatted bytes. */
    PublicKey createPublicKey(KeyFormat container, byte[] value);
    /** Imports a private key from the preferred PKCS #8 PEM format. */
    PrivateKey createPrivateKey(String value);
    /** Imports a public key from the preferred SPKI PEM format. */
    PublicKey createPublicKey(String value);
}
