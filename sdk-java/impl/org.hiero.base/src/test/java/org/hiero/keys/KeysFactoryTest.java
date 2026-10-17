package org.hiero.keys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `KeysFactory`, generated from the specs.
@DisplayName("KeysFactory")
class KeysFactoryTest {

    @Test
    @DisplayName("generatePrivateKey(algorithm) can be called and returns a value")
    @Tag("testId:keys/generatePrivateKey/call")
    void shouldCallGeneratePrivateKey() {
        assertNotNull(KeysFactory.generatePrivateKey(algorithmValue()));
    }

    @Test
    @DisplayName("generatePrivateKey rejects null for algorithm")
    @Tag("testId:keys/generatePrivateKey/algorithm/rejects-null")
    void shouldRejectNullAlgorithmInGeneratePrivateKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.generatePrivateKey((KeyAlgorithm) null));
    }

    @Test
    @DisplayName("generatePublicKey(algorithm) can be called and returns a value")
    @Tag("testId:keys/generatePublicKey/call")
    void shouldCallGeneratePublicKey() {
        assertNotNull(KeysFactory.generatePublicKey(algorithmValue()));
    }

    @Test
    @DisplayName("generatePublicKey rejects null for algorithm")
    @Tag("testId:keys/generatePublicKey/algorithm/rejects-null")
    void shouldRejectNullAlgorithmInGeneratePublicKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.generatePublicKey((KeyAlgorithm) null));
    }

    @Test
    @DisplayName("createPrivateKey(algorithm, encoding, value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPrivateKey/call")
    void shouldCallCreatePrivateKey() throws Exception {
        try {
            assertNotNull(KeysFactory.createPrivateKey(algorithmValue(), encodingValue(), valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPrivateKey rejects null for algorithm")
    @Tag("testId:keys/createPrivateKey/algorithm/rejects-null")
    void shouldRejectNullAlgorithmInCreatePrivateKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey((KeyAlgorithm) null, encodingValue(), valueValue()));
    }

    @Test
    @DisplayName("createPrivateKey rejects null for encoding")
    @Tag("testId:keys/createPrivateKey/encoding/rejects-null")
    void shouldRejectNullEncodingInCreatePrivateKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey(algorithmValue(), (ByteImportEncoding) null, valueValue()));
    }

    @Test
    @DisplayName("createPrivateKey rejects null for value")
    @Tag("testId:keys/createPrivateKey/value/rejects-null")
    void shouldRejectNullValueInCreatePrivateKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey(algorithmValue(), encodingValue(), (String) null));
    }

    @Test
    @DisplayName("createPublicKey(algorithm, encoding, value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPublicKey/call")
    void shouldCallCreatePublicKey() throws Exception {
        try {
            assertNotNull(KeysFactory.createPublicKey(algorithmValue(), encodingValue(), valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPublicKey rejects null for algorithm")
    @Tag("testId:keys/createPublicKey/algorithm/rejects-null")
    void shouldRejectNullAlgorithmInCreatePublicKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey((KeyAlgorithm) null, encodingValue(), valueValue()));
    }

    @Test
    @DisplayName("createPublicKey rejects null for encoding")
    @Tag("testId:keys/createPublicKey/encoding/rejects-null")
    void shouldRejectNullEncodingInCreatePublicKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey(algorithmValue(), (ByteImportEncoding) null, valueValue()));
    }

    @Test
    @DisplayName("createPublicKey rejects null for value")
    @Tag("testId:keys/createPublicKey/value/rejects-null")
    void shouldRejectNullValueInCreatePublicKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey(algorithmValue(), encodingValue(), (String) null));
    }

    @Test
    @DisplayName("createPrivateKey(algorithm, rawBytes) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPrivateKey/call-2")
    void shouldCallCreatePrivateKey2() throws Exception {
        try {
            assertNotNull(KeysFactory.createPrivateKey(algorithmValue(), rawBytesValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPrivateKey rejects null for algorithm")
    @Tag("testId:keys/createPrivateKey/algorithm/rejects-null-2")
    void shouldRejectNullAlgorithmInCreatePrivateKey2() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey((KeyAlgorithm) null, rawBytesValue()));
    }

    @Test
    @DisplayName("createPrivateKey rejects null for rawBytes")
    @Tag("testId:keys/createPrivateKey/rawBytes/rejects-null")
    void shouldRejectNullRawBytesInCreatePrivateKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey(algorithmValue(), (byte[]) null));
    }

    @Test
    @DisplayName("createPublicKey(algorithm, rawBytes) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPublicKey/call-2")
    void shouldCallCreatePublicKey2() throws Exception {
        try {
            assertNotNull(KeysFactory.createPublicKey(algorithmValue(), rawBytesValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPublicKey rejects null for algorithm")
    @Tag("testId:keys/createPublicKey/algorithm/rejects-null-2")
    void shouldRejectNullAlgorithmInCreatePublicKey2() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey((KeyAlgorithm) null, rawBytesValue()));
    }

    @Test
    @DisplayName("createPublicKey rejects null for rawBytes")
    @Tag("testId:keys/createPublicKey/rawBytes/rejects-null")
    void shouldRejectNullRawBytesInCreatePublicKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey(algorithmValue(), (byte[]) null));
    }

    @Test
    @DisplayName("createPrivateKey(container, value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPrivateKey/call-3")
    void shouldCallCreatePrivateKey3() throws Exception {
        try {
            assertNotNull(KeysFactory.createPrivateKey(containerValue(), valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPrivateKey rejects null for container")
    @Tag("testId:keys/createPrivateKey/container/rejects-null")
    void shouldRejectNullContainerInCreatePrivateKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey((KeyFormat) null, valueValue()));
    }

    @Test
    @DisplayName("createPrivateKey rejects null for value")
    @Tag("testId:keys/createPrivateKey/value/rejects-null-2")
    void shouldRejectNullValueInCreatePrivateKey2() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey(containerValue(), (String) null));
    }

    @Test
    @DisplayName("createPublicKey(container, value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPublicKey/call-3")
    void shouldCallCreatePublicKey3() throws Exception {
        try {
            assertNotNull(KeysFactory.createPublicKey(containerValue(), valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPublicKey rejects null for container")
    @Tag("testId:keys/createPublicKey/container/rejects-null")
    void shouldRejectNullContainerInCreatePublicKey() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey((KeyFormat) null, valueValue()));
    }

    @Test
    @DisplayName("createPublicKey rejects null for value")
    @Tag("testId:keys/createPublicKey/value/rejects-null-2")
    void shouldRejectNullValueInCreatePublicKey2() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey(containerValue(), (String) null));
    }

    @Test
    @DisplayName("createPrivateKey(container, value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPrivateKey/call-4")
    void shouldCallCreatePrivateKey4() throws Exception {
        try {
            assertNotNull(KeysFactory.createPrivateKey(containerValue(), valueValue2()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPrivateKey rejects null for container")
    @Tag("testId:keys/createPrivateKey/container/rejects-null-2")
    void shouldRejectNullContainerInCreatePrivateKey2() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey((KeyFormat) null, valueValue2()));
    }

    @Test
    @DisplayName("createPrivateKey rejects null for value")
    @Tag("testId:keys/createPrivateKey/value/rejects-null-3")
    void shouldRejectNullValueInCreatePrivateKey3() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey(containerValue(), (byte[]) null));
    }

    @Test
    @DisplayName("createPublicKey(container, value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPublicKey/call-4")
    void shouldCallCreatePublicKey4() throws Exception {
        try {
            assertNotNull(KeysFactory.createPublicKey(containerValue(), valueValue2()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPublicKey rejects null for container")
    @Tag("testId:keys/createPublicKey/container/rejects-null-2")
    void shouldRejectNullContainerInCreatePublicKey2() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey((KeyFormat) null, valueValue2()));
    }

    @Test
    @DisplayName("createPublicKey rejects null for value")
    @Tag("testId:keys/createPublicKey/value/rejects-null-3")
    void shouldRejectNullValueInCreatePublicKey3() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey(containerValue(), (byte[]) null));
    }

    @Test
    @DisplayName("createPrivateKey(value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPrivateKey/call-5")
    void shouldCallCreatePrivateKey5() throws Exception {
        try {
            assertNotNull(KeysFactory.createPrivateKey(valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPrivateKey rejects null for value")
    @Tag("testId:keys/createPrivateKey/value/rejects-null-4")
    void shouldRejectNullValueInCreatePrivateKey4() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPrivateKey((String) null));
    }

    @Test
    @DisplayName("createPublicKey(value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys/createPublicKey/call-5")
    void shouldCallCreatePublicKey5() throws Exception {
        try {
            assertNotNull(KeysFactory.createPublicKey(valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createPublicKey rejects null for value")
    @Tag("testId:keys/createPublicKey/value/rejects-null-4")
    void shouldRejectNullValueInCreatePublicKey4() {
        assertThrows(NullPointerException.class, () -> KeysFactory.createPublicKey((String) null));
    }

    private static KeyAlgorithm algorithmValue() {
        return KeyAlgorithm.ED25519;
    }

    private static ByteImportEncoding encodingValue() {
        return ByteImportEncoding.HEX;
    }

    private static String valueValue() {
        return "value";
    }

    private static byte[] rawBytesValue() {
        return new byte[] {1, 2, 3};
    }

    private static KeyFormat containerValue() {
        return KeyFormat.PKCS8_WITH_DER;
    }

    private static byte[] valueValue2() {
        return new byte[] {1, 2, 3};
    }
}
