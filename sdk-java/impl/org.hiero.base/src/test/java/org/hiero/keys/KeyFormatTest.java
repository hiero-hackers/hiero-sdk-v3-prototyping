package org.hiero.keys;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `KeyFormat`, generated from the specs.
@DisplayName("KeyFormat")
class KeyFormatTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:keys.KeyFormat/constants")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(KeyContainer.PKCS8, KeyFormat.PKCS8_WITH_DER.container());
        assertValue(KeyEncoding.DER, KeyFormat.PKCS8_WITH_DER.encoding());
        assertValue(KeyContainer.SPKI, KeyFormat.SPKI_WITH_DER.container());
        assertValue(KeyEncoding.DER, KeyFormat.SPKI_WITH_DER.encoding());
        assertValue(KeyContainer.PKCS8, KeyFormat.PKCS8_WITH_PEM.container());
        assertValue(KeyEncoding.PEM, KeyFormat.PKCS8_WITH_PEM.encoding());
        assertValue(KeyContainer.SPKI, KeyFormat.SPKI_WITH_PEM.container());
        assertValue(KeyEncoding.PEM, KeyFormat.SPKI_WITH_PEM.encoding());
    }

    @Test
    @DisplayName("supportsType(type) can be called")
    @Tag("testId:keys.KeyFormat/supportsType/call")
    void shouldCallSupportsType() {
        KeyFormat.PKCS8_WITH_DER.supportsType(typeValue());
    }

    @Test
    @DisplayName("decode(keyType, value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:keys.KeyFormat/decode/call")
    void shouldCallDecode() throws Exception {
        try {
            assertNotNull(KeyFormat.PKCS8_WITH_DER.decode(keyTypeValue(), valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static KeyType typeValue() {
        return KeyType.PUBLIC;
    }

    private static KeyType keyTypeValue() {
        return KeyType.PUBLIC;
    }

    private static String valueValue() {
        return "value";
    }
}
