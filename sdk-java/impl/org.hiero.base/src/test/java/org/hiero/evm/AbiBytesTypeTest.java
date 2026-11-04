package org.hiero.evm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiBytesType`, generated from the specs.
@DisplayName("AbiBytesType")
class AbiBytesTypeTest {

    @Test
    @DisplayName("creates an AbiBytesType and returns the values")
    @Tag("testId:evm.AbiBytesType/create")
    void shouldCreateAndReturnTheValues() {
        final AbiBytesType subject = new AbiBytesType();
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiBytesType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static AbiBytesType create() {
        return new AbiBytesType();
    }
}
