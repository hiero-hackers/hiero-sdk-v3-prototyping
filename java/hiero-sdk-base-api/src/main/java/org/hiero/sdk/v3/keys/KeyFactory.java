// Generated from spec/base/keys.md (keys#KeyFactory); DO NOT EDIT.

package org.hiero.sdk.v3.keys;

/** Body-free key generation and import contract. */
public interface KeyFactory {
    PrivateKey generatePrivateKey(KeyAlgorithm algorithm);
    PublicKey generatePublicKey(KeyAlgorithm algorithm);
    PrivateKey createPrivateKey(KeyAlgorithm algorithm, ByteImportEncoding encoding, String value);
    PublicKey createPublicKey(KeyAlgorithm algorithm, ByteImportEncoding encoding, String value);
    PrivateKey createPrivateKey(KeyAlgorithm algorithm, byte[] rawBytes);
    PublicKey createPublicKey(KeyAlgorithm algorithm, byte[] rawBytes);
    PrivateKey createPrivateKey(KeyFormat container, String value);
    PublicKey createPublicKey(KeyFormat container, String value);
    PrivateKey createPrivateKey(KeyFormat container, byte[] value);
    PublicKey createPublicKey(KeyFormat container, byte[] value);
    PrivateKey createPrivateKey(String value);
    PublicKey createPublicKey(String value);
}
