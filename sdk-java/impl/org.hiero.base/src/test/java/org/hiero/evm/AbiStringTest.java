package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiString`, generated from the specs.
@DisplayName("AbiString")
class AbiStringTest {

    @Test
    @DisplayName("creates an AbiString and returns the values")
    @Tag("testId:evm.AbiString/create")
    void shouldCreateAndReturnTheValues() {
        final String valueArgument = valueValue();
        final AbiString subject = new AbiString(valueArgument);
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("rejects null for value")
    @Tag("testId:evm.AbiString/value/rejects-null")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new AbiString((String) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiString/value-semantics")
    void shouldCompareByValue() {
        final AbiString subject = create();
        final AbiString other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiString/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    private static String valueValue() {
        return "value";
    }

    private static AbiString create() {
        return new AbiString(valueValue());
    }
}
