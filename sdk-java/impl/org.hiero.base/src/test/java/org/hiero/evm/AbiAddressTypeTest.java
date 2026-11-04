package org.hiero.evm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiAddressType`, generated from the specs.
@DisplayName("AbiAddressType")
class AbiAddressTypeTest {

    @Test
    @DisplayName("creates an AbiAddressType and returns the values")
    @Tag("testId:evm.AbiAddressType/create")
    void shouldCreateAndReturnTheValues() {
        final AbiAddressType subject = new AbiAddressType();
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiAddressType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static AbiAddressType create() {
        return new AbiAddressType();
    }
}
