package org.hiero.keys;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `KeyPair`, generated from the specs.
@DisplayName("KeyPair")
class KeyPairTest {

    @Test
    @DisplayName("creates a KeyPair and returns the values")
    void shouldCreateAndReturnTheValues() {
        final PublicKey publicKey = publicKeyValue();
        final PrivateKey privateKey = privateKeyValue();
        final KeyPair subject = new KeyPair(publicKey, privateKey);
        assertValue(publicKey, subject.publicKey());
        assertValue(privateKey, subject.privateKey());
    }

    @Test
    @DisplayName("rejects null for publicKey")
    void shouldRejectNullPublicKey() {
        assertThrows(NullPointerException.class, () -> new KeyPair((PublicKey) null, privateKeyValue()));
    }

    @Test
    @DisplayName("rejects null for privateKey")
    void shouldRejectNullPrivateKey() {
        assertThrows(NullPointerException.class, () -> new KeyPair(publicKeyValue(), (PrivateKey) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final KeyPair subject = create();
        final KeyPair other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static PublicKey publicKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey();
    }

    private static PrivateKey privateKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static KeyPair create() {
        return new KeyPair(publicKeyValue(), privateKeyValue());
    }
}
