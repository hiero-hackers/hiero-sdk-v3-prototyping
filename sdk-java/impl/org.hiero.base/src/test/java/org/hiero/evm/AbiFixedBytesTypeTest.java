package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiFixedBytesType`, generated from the specs.
@DisplayName("AbiFixedBytesType")
class AbiFixedBytesTypeTest {

    @Test
    @DisplayName("creates an AbiFixedBytesType and returns the values")
    @Tag("testId:evm.AbiFixedBytesType/create")
    void shouldCreateAndReturnTheValues() {
        final short size = sizeValue();
        final AbiFixedBytesType subject = new AbiFixedBytesType(size);
        assertValue(size, subject.size());
    }

    @Test
    @DisplayName("size: accepts the minimum (1)")
    @Tag("testId:evm.AbiFixedBytesType/size/accepts-at-minimum")
    void shouldAcceptSizeAtMinimum() {
        final short value = (short) 1;
        final AbiFixedBytesType subject = new AbiFixedBytesType(value);
        assertValue(value, subject.size());
    }

    @Test
    @DisplayName("size: accepts the maximum (32)")
    @Tag("testId:evm.AbiFixedBytesType/size/accepts-at-maximum")
    void shouldAcceptSizeAtMaximum() {
        final short value = (short) 32;
        final AbiFixedBytesType subject = new AbiFixedBytesType(value);
        assertValue(value, subject.size());
    }

    @Test
    @DisplayName("size: rejects a value below the minimum (0)")
    @Tag("testId:evm.AbiFixedBytesType/size/rejects-below-minimum")
    void shouldRejectSizeBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiFixedBytesType((short) 0));
    }

    @Test
    @DisplayName("size: rejects a value above the maximum (33)")
    @Tag("testId:evm.AbiFixedBytesType/size/rejects-above-maximum")
    void shouldRejectSizeAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiFixedBytesType((short) 33));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiFixedBytesType/value-semantics")
    void shouldCompareByValue() {
        final AbiFixedBytesType subject = create();
        final AbiFixedBytesType other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiFixedBytesType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static short sizeValue() {
        return (short) 1;
    }

    private static AbiFixedBytesType create() {
        return new AbiFixedBytesType(sizeValue());
    }
}
