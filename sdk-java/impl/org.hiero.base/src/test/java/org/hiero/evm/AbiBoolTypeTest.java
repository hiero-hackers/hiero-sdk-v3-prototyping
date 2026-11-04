package org.hiero.evm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiBoolType`, generated from the specs.
@DisplayName("AbiBoolType")
class AbiBoolTypeTest {

    @Test
    @DisplayName("creates an AbiBoolType and returns the values")
    @Tag("testId:evm.AbiBoolType/create")
    void shouldCreateAndReturnTheValues() {
        final AbiBoolType subject = new AbiBoolType();
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiBoolType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static AbiBoolType create() {
        return new AbiBoolType();
    }
}
