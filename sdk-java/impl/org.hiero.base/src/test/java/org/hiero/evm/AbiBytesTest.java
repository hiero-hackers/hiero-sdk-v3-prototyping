package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiBytes`, generated from the specs.
@DisplayName("AbiBytes")
class AbiBytesTest {

    @Test
    @DisplayName("creates an AbiBytes and returns the values")
    @Tag("testId:evm.AbiBytes/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] valueArgument = valueValue();
        final AbiBytes subject = new AbiBytes(valueArgument);
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("rejects null for value")
    @Tag("testId:evm.AbiBytes/value/rejects-null")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new AbiBytes((byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies value; the accessor returns a copy")
    @Tag("testId:evm.AbiBytes/value/copies-in")
    void shouldCopyValueIn() {
        final byte[] value = valueValue();
        final byte[] expected = value.clone();
        final AbiBytes subject = new AbiBytes(value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.value());
        final byte[] returned = subject.value();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.value());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiBytes/value-semantics")
    void shouldCompareByValue() {
        final AbiBytes subject = create();
        final AbiBytes other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiBytes/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    private static byte[] valueValue() {
        return new byte[] {1, 2, 3};
    }

    private static AbiBytes create() {
        return new AbiBytes(valueValue());
    }
}
