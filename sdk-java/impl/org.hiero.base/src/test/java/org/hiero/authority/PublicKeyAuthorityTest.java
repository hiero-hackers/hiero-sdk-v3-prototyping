package org.hiero.authority;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PublicKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `PublicKeyAuthority`, generated from the specs.
@DisplayName("PublicKeyAuthority")
class PublicKeyAuthorityTest {

    @Test
    @DisplayName("creates a PublicKeyAuthority and returns the values")
    @Tag("testId:authority.PublicKeyAuthority/create")
    void shouldCreateAndReturnTheValues() {
        final PublicKey publicKey = publicKeyValue();
        final PublicKeyAuthority subject = new PublicKeyAuthority(publicKey);
        assertValue(publicKey, subject.publicKey());
    }

    @Test
    @DisplayName("rejects null for publicKey")
    @Tag("testId:authority.PublicKeyAuthority/publicKey/rejects-null")
    void shouldRejectNullPublicKey() {
        assertThrows(NullPointerException.class, () -> new PublicKeyAuthority((PublicKey) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:authority.PublicKeyAuthority/value-semantics")
    void shouldCompareByValue() {
        final PublicKeyAuthority subject = create();
        final PublicKeyAuthority other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static PublicKey publicKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey();
    }

    private static PublicKeyAuthority create() {
        return new PublicKeyAuthority(publicKeyValue());
    }
}
