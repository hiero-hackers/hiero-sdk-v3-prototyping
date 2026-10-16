package org.hiero.keys;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `KeyEncoding`, generated from the specs.
@DisplayName("KeyEncoding")
class KeyEncodingTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(RawFormat.BYTES, KeyEncoding.DER.rawFormat());
        assertValue(RawFormat.STRING, KeyEncoding.PEM.rawFormat());
    }

    @Test
    @DisplayName("decode(keyType, value) can be called and returns a value (only IllegalArgumentException may occur)")
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

    /// Asserts that two values are equal; arrays by their content.
    private static void assertValue(final @Nullable Object expected, final @Nullable Object actual) {
        if (expected instanceof byte[] bytes && actual instanceof byte[] other) {
            assertArrayEquals(bytes, other, () -> "expected " + Arrays.toString(bytes) + " but was " + Arrays.toString(other));
        } else {
            assertEquals(expected, actual);
        }
    }
}
