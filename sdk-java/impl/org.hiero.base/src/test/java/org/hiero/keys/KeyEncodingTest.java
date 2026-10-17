package org.hiero.keys;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `KeyEncoding`, generated from the specs.
@DisplayName("KeyEncoding")
class KeyEncodingTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:keys.KeyEncoding/constants")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(RawFormat.BYTES, KeyEncoding.DER.rawFormat());
        assertValue(RawFormat.STRING, KeyEncoding.PEM.rawFormat());
    }

    @Test
    @DisplayName("decode(keyType, value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys.KeyEncoding/decode/call")
    void shouldCallDecode() throws Exception {
        try {
            assertNotNull(KeyEncoding.DER.decode(keyTypeValue(), valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static KeyType keyTypeValue() {
        return KeyType.PUBLIC;
    }

    private static String valueValue() {
        return "value";
    }
}
