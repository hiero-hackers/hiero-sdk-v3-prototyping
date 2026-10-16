package org.hiero.keys;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `PrivateKey`, generated from the specs.
@DisplayName("PrivateKey")
class PrivateKeyTest {

    @Test
    @DisplayName("creates a PrivateKey and returns the values")
    void shouldCreateAndReturnTheValues() {
        final byte[] bytes = bytesValue();
        final KeyAlgorithm algorithm = algorithmValue();
        final KeyType type = typeValue();
        final PrivateKey subject = new PrivateKey(bytes, algorithm, type);
        assertValue(bytes, subject.bytes());
        assertValue(algorithm, subject.algorithm());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("rejects null for bytes")
    void shouldRejectNullBytes() {
        assertThrows(NullPointerException.class, () -> new PrivateKey((byte[]) null, algorithmValue(), typeValue()));
    }

    @Test
    @DisplayName("the constructor copies bytes; the accessor returns a copy")
    void shouldCopyBytesIn() {
        final byte[] value = bytesValue();
        final byte[] expected = value.clone();
        final PrivateKey subject = new PrivateKey(value, algorithmValue(), typeValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bytes());
        final byte[] returned = subject.bytes();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bytes());
    }

    @Test
    @DisplayName("rejects null for algorithm")
    void shouldRejectNullAlgorithm() {
        assertThrows(NullPointerException.class, () -> new PrivateKey(bytesValue(), (KeyAlgorithm) null, typeValue()));
    }

    @Test
    @DisplayName("rejects null for type")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new PrivateKey(bytesValue(), algorithmValue(), (KeyType) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final PrivateKey subject = create();
        final PrivateKey other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("toRawBytes() can be called and returns a value")
    void shouldCallToRawBytes() {
        assertNotNull(defaultSubject().toRawBytes());
    }

    @Test
    @DisplayName("toBytes(container) can be called and returns a value (only IllegalArgumentException may occur)")
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
    @DisplayName("sign(message) can be called and returns a value")
    void shouldCallSign() {
        assertNotNull(defaultSubject().sign(messageValue()));
    }

    @Test
    @DisplayName("createPublicKey() can be called and returns a value")
    void shouldCallCreatePublicKey() {
        assertNotNull(defaultSubject().createPublicKey());
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

    private static PrivateKey create() {
        return new PrivateKey(bytesValue(), algorithmValue(), typeValue());
    }

    private static PrivateKey defaultSubject() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static KeyFormat containerValue() {
        return KeyFormat.PKCS8_WITH_DER;
    }

    private static byte[] messageValue() {
        return new byte[] {1, 2, 3};
    }

    /// Asserts that two values are equal; arrays by their content.
    private static void assertValue(final @Nullable Object expected, final @Nullable Object actual) {
        if (expected instanceof byte[] bytes && actual instanceof byte[] other) {
            assertArrayEquals(bytes, other, () -> "expected " + Arrays.toString(bytes) + " but was " + Arrays.toString(other));
        } else {
            assertEquals(expected, actual);
        }
    }
}
