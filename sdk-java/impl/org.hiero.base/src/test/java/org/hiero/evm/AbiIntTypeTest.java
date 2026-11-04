package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiIntType`, generated from the specs.
@DisplayName("AbiIntType")
class AbiIntTypeTest {

    @Test
    @DisplayName("creates an AbiIntType and returns the values")
    @Tag("testId:evm.AbiIntType/create")
    void shouldCreateAndReturnTheValues() {
        final int bits = bitsValue();
        final AbiIntType subject = new AbiIntType(bits);
        assertValue(bits, subject.bits());
    }

    @Test
    @DisplayName("creates an AbiIntType with the default values")
    @Tag("testId:evm.AbiIntType/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AbiIntType subject = new AbiIntType();
        assertValue(256, subject.bits());
    }

    @Test
    @DisplayName("bits: accepts the minimum (8)")
    @Tag("testId:evm.AbiIntType/bits/accepts-at-minimum")
    void shouldAcceptBitsAtMinimum() {
        final int value = 8;
        final AbiIntType subject = new AbiIntType(value);
        assertValue(value, subject.bits());
    }

    @Test
    @DisplayName("bits: accepts the maximum (256)")
    @Tag("testId:evm.AbiIntType/bits/accepts-at-maximum")
    void shouldAcceptBitsAtMaximum() {
        final int value = 256;
        final AbiIntType subject = new AbiIntType(value);
        assertValue(value, subject.bits());
    }

    @Test
    @DisplayName("bits: rejects a value below the minimum (7)")
    @Tag("testId:evm.AbiIntType/bits/rejects-below-minimum")
    void shouldRejectBitsBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiIntType(7));
    }

    @Test
    @DisplayName("bits: rejects a value above the maximum (257)")
    @Tag("testId:evm.AbiIntType/bits/rejects-above-maximum")
    void shouldRejectBitsAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiIntType(257));
    }

    @Test
    @DisplayName("bits: rejects a value that is no multiple (9)")
    @Tag("testId:evm.AbiIntType/bits/rejects-not-amultiple")
    void shouldRejectBitsNotAMultiple() {
        assertThrows(IllegalArgumentException.class, () -> new AbiIntType(9));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiIntType/value-semantics")
    void shouldCompareByValue() {
        final AbiIntType subject = create();
        final AbiIntType other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiIntType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static int bitsValue() {
        return 8;
    }

    private static AbiIntType create() {
        return new AbiIntType(bitsValue());
    }
}
