package org.hiero.evm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiType`, generated from the specs.
@DisplayName("AbiType")
class AbiTypeTest {

    @Test
    @DisplayName("parse(text) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiType/parse/call")
    void shouldCallParse() throws Exception {
        try {
            assertNotNull(AbiType.parse(textValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static String textValue() {
        return "value";
    }
}
