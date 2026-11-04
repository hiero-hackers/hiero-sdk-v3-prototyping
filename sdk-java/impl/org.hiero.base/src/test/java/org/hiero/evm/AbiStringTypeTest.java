package org.hiero.evm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiStringType`, generated from the specs.
@DisplayName("AbiStringType")
class AbiStringTypeTest {

    @Test
    @DisplayName("creates an AbiStringType and returns the values")
    @Tag("testId:evm.AbiStringType/create")
    void shouldCreateAndReturnTheValues() {
        final AbiStringType subject = new AbiStringType();
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiStringType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static AbiStringType create() {
        return new AbiStringType();
    }
}
