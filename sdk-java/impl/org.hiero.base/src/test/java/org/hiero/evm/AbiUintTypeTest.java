package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiUintType`, generated from the specs.
@DisplayName("AbiUintType")
class AbiUintTypeTest {

    @Test
    @DisplayName("creates an AbiUintType and returns the values")
    @Tag("testId:evm.AbiUintType/create")
    void shouldCreateAndReturnTheValues() {
        final int bits = bitsValue();
        final AbiUintType subject = new AbiUintType(bits);
        assertValue(bits, subject.bits());
    }

    @Test
    @DisplayName("creates an AbiUintType with the default values")
    @Tag("testId:evm.AbiUintType/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AbiUintType subject = new AbiUintType();
        assertValue(256, subject.bits());
    }

    @Test
    @DisplayName("bits: accepts the minimum (8)")
    @Tag("testId:evm.AbiUintType/bits/accepts-at-minimum")
    void shouldAcceptBitsAtMinimum() {
        final int value = 8;
        final AbiUintType subject = new AbiUintType(value);
        assertValue(value, subject.bits());
    }

    @Test
    @DisplayName("bits: accepts the maximum (256)")
    @Tag("testId:evm.AbiUintType/bits/accepts-at-maximum")
    void shouldAcceptBitsAtMaximum() {
        final int value = 256;
        final AbiUintType subject = new AbiUintType(value);
        assertValue(value, subject.bits());
    }

    @Test
    @DisplayName("bits: rejects a value below the minimum (7)")
    @Tag("testId:evm.AbiUintType/bits/rejects-below-minimum")
    void shouldRejectBitsBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiUintType(7));
    }

    @Test
    @DisplayName("bits: rejects a value above the maximum (257)")
    @Tag("testId:evm.AbiUintType/bits/rejects-above-maximum")
    void shouldRejectBitsAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiUintType(257));
    }

    @Test
    @DisplayName("bits: rejects a value that is no multiple (9)")
    @Tag("testId:evm.AbiUintType/bits/rejects-not-amultiple")
    void shouldRejectBitsNotAMultiple() {
        assertThrows(IllegalArgumentException.class, () -> new AbiUintType(9));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiUintType/value-semantics")
    void shouldCompareByValue() {
        final AbiUintType subject = create();
        final AbiUintType other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiUintType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static int bitsValue() {
        return 8;
    }

    private static AbiUintType create() {
        return new AbiUintType(bitsValue());
    }
}
