package org.hiero.keys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `ByteImportEncoding`, generated from the specs.
@DisplayName("ByteImportEncoding")
class ByteImportEncodingTest {

    @Test
    @DisplayName("decode(value) can be called and returns a value (only IllegalArgumentException may occur)")
    void shouldCallDecode() throws Exception {
        try {
            assertNotNull(ByteImportEncoding.HEX.decode(valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static String valueValue() {
        return "value";
    }
}
