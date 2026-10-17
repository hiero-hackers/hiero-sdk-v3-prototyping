package org.hiero.keys;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `PublicKey`, generated from the specs.
@DisplayName("PublicKey")
class PublicKeyTest {

    @Test
    @DisplayName("creates a PublicKey and returns the values")
    @Tag("testId:keys.PublicKey/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] bytes = bytesValue();
        final KeyAlgorithm algorithm = algorithmValue();
        final KeyType type = typeValue();
        final PublicKey subject = new PublicKey(bytes, algorithm, type);
        assertValue(bytes, subject.bytes());
        assertValue(algorithm, subject.algorithm());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("rejects null for bytes")
    @Tag("testId:keys.PublicKey/bytes/rejects-null")
    void shouldRejectNullBytes() {
        assertThrows(NullPointerException.class, () -> new PublicKey((byte[]) null, algorithmValue(), typeValue()));
    }

    @Test
    @DisplayName("the constructor copies bytes; the accessor returns a copy")
    @Tag("testId:keys.PublicKey/bytes/copies-in")
    void shouldCopyBytesIn() {
        final byte[] value = bytesValue();
        final byte[] expected = value.clone();
        final PublicKey subject = new PublicKey(value, algorithmValue(), typeValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bytes());
        final byte[] returned = subject.bytes();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bytes());
    }

    @Test
    @DisplayName("rejects null for algorithm")
    @Tag("testId:keys.PublicKey/algorithm/rejects-null")
    void shouldRejectNullAlgorithm() {
        assertThrows(NullPointerException.class, () -> new PublicKey(bytesValue(), (KeyAlgorithm) null, typeValue()));
    }

    @Test
    @DisplayName("rejects null for type")
    @Tag("testId:keys.PublicKey/type/rejects-null")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new PublicKey(bytesValue(), algorithmValue(), (KeyType) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:keys.PublicKey/value-semantics")
    void shouldCompareByValue() {
        final PublicKey subject = create();
        final PublicKey other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("toRawBytes() can be called and returns a value")
    @Tag("testId:keys.PublicKey/toRawBytes/call")
    void shouldCallToRawBytes() {
        assertNotNull(defaultSubject().toRawBytes());
    }

    @Test
    @DisplayName("toBytes(container) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys.PublicKey/toBytes/call")
    void shouldCallToBytes() throws Exception {
        try {
            assertNotNull(defaultSubject().toBytes(containerValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("toString(container) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys.PublicKey/toString/call")
    void shouldCallToString() throws Exception {
        try {
            assertNotNull(defaultSubject().toString(containerValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("verify(message, signature) can be called")
    @Tag("testId:keys.PublicKey/verify/call")
    void shouldCallVerify() {
        defaultSubject().verify(messageValue(), signatureValue());
    }

    private static byte[] bytesValue() {
        return new byte[] {1, 2, 3};
    }

    private static KeyAlgorithm algorithmValue() {
        return KeyAlgorithm.ED25519;
    }

    private static KeyType typeValue() {
        return KeyType.PUBLIC;
    }

    private static PublicKey create() {
        return new PublicKey(bytesValue(), algorithmValue(), typeValue());
    }

    private static PublicKey defaultSubject() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey();
    }

    private static KeyFormat containerValue() {
        return KeyFormat.PKCS8_WITH_DER;
    }

    private static byte[] messageValue() {
        return new byte[] {1, 2, 3};
    }

    private static byte[] signatureValue() {
        return new byte[] {1, 2, 3};
    }
}
