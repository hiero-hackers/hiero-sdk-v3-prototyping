package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiFixedBytes`, generated from the specs.
@DisplayName("AbiFixedBytes")
class AbiFixedBytesTest {

    @Test
    @DisplayName("creates an AbiFixedBytes and returns the values")
    @Tag("testId:evm.AbiFixedBytes/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] valueArgument = valueValue();
        final AbiFixedBytes subject = new AbiFixedBytes(valueArgument);
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("rejects null for value")
    @Tag("testId:evm.AbiFixedBytes/value/rejects-null")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new AbiFixedBytes((byte[]) null));
    }

    @Test
    @DisplayName("value: accepts the minimum number of elements")
    @Tag("testId:evm.AbiFixedBytes/value/accepts-at-minimum-size")
    void shouldAcceptValueAtMinimumSize() {
        final byte[] value = new byte[] {1};
        final AbiFixedBytes subject = new AbiFixedBytes(value);
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: rejects fewer elements than the minimum")
    @Tag("testId:evm.AbiFixedBytes/value/rejects-below-minimum-size")
    void shouldRejectValueBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new AbiFixedBytes(new byte[0]));
    }

    @Test
    @DisplayName("value: accepts the maximum number of elements")
    @Tag("testId:evm.AbiFixedBytes/value/accepts-at-maximum-size")
    void shouldAcceptValueAtMaximumSize() {
        final byte[] value = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        final AbiFixedBytes subject = new AbiFixedBytes(value);
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: rejects more elements than the maximum")
    @Tag("testId:evm.AbiFixedBytes/value/rejects-above-maximum-size")
    void shouldRejectValueAboveMaximumSize() {
        assertThrows(IllegalArgumentException.class, () -> new AbiFixedBytes(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}));
    }

    @Test
    @DisplayName("the constructor copies value; the accessor returns a copy")
    @Tag("testId:evm.AbiFixedBytes/value/copies-in")
    void shouldCopyValueIn() {
        final byte[] value = valueValue();
        final byte[] expected = value.clone();
        final AbiFixedBytes subject = new AbiFixedBytes(value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.value());
        final byte[] returned = subject.value();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.value());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiFixedBytes/value-semantics")
    void shouldCompareByValue() {
        final AbiFixedBytes subject = create();
        final AbiFixedBytes other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiFixedBytes/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    private static byte[] valueValue() {
        return new byte[] {1, 2, 3};
    }

    private static AbiFixedBytes create() {
        return new AbiFixedBytes(valueValue());
    }
}
