package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiBool`, generated from the specs.
@DisplayName("AbiBool")
class AbiBoolTest {

    @Test
    @DisplayName("creates an AbiBool and returns the values")
    @Tag("testId:evm.AbiBool/create")
    void shouldCreateAndReturnTheValues() {
        final boolean valueArgument = valueValue();
        final AbiBool subject = new AbiBool(valueArgument);
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiBool/value-semantics")
    void shouldCompareByValue() {
        final AbiBool subject = create();
        final AbiBool other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiBool/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    private static boolean valueValue() {
        return true;
    }

    private static AbiBool create() {
        return new AbiBool(valueValue());
    }
}
