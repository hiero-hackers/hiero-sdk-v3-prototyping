package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiInt`, generated from the specs.
@DisplayName("AbiInt")
class AbiIntTest {

    @Test
    @DisplayName("creates an AbiInt and returns the values")
    @Tag("testId:evm.AbiInt/create")
    void shouldCreateAndReturnTheValues() {
        final BigInteger valueArgument = valueValue();
        final int bits = bitsValue();
        final AbiInt subject = new AbiInt(valueArgument, bits);
        assertValue(valueArgument, subject.value());
        assertValue(bits, subject.bits());
    }

    @Test
    @DisplayName("creates an AbiInt with the default values")
    @Tag("testId:evm.AbiInt/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AbiInt subject = new AbiInt(valueValue());
        assertValue(256, subject.bits());
    }

    @Test
    @DisplayName("rejects null for value")
    @Tag("testId:evm.AbiInt/value/rejects-null")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new AbiInt((BigInteger) null, bitsValue()));
    }

    @Test
    @DisplayName("value: accepts the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:evm.AbiInt/value/accepts-at-minimum")
    void shouldAcceptValueAtMinimum() {
        final BigInteger value = new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968");
        final AbiInt subject = new AbiInt(value, bitsValue());
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: accepts the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819967)")
    @Tag("testId:evm.AbiInt/value/accepts-at-maximum")
    void shouldAcceptValueAtMaximum() {
        final BigInteger value = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967");
        final AbiInt subject = new AbiInt(value, bitsValue());
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: rejects a value below the minimum (-57896044618658097711785492504343953926634992332820282019728792003956564819969)")
    @Tag("testId:evm.AbiInt/value/rejects-below-minimum")
    void shouldRejectValueBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiInt(new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819969"), bitsValue()));
    }

    @Test
    @DisplayName("value: rejects a value above the maximum (57896044618658097711785492504343953926634992332820282019728792003956564819968)")
    @Tag("testId:evm.AbiInt/value/rejects-above-maximum")
    void shouldRejectValueAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiInt(new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819968"), bitsValue()));
    }

    @Test
    @DisplayName("bits: accepts the minimum (8)")
    @Tag("testId:evm.AbiInt/bits/accepts-at-minimum")
    void shouldAcceptBitsAtMinimum() {
        final int value = 8;
        final AbiInt subject = new AbiInt(valueValue(), value);
        assertValue(value, subject.bits());
    }

    @Test
    @DisplayName("bits: accepts the maximum (256)")
    @Tag("testId:evm.AbiInt/bits/accepts-at-maximum")
    void shouldAcceptBitsAtMaximum() {
        final int value = 256;
        final AbiInt subject = new AbiInt(valueValue(), value);
        assertValue(value, subject.bits());
    }

    @Test
    @DisplayName("bits: rejects a value below the minimum (7)")
    @Tag("testId:evm.AbiInt/bits/rejects-below-minimum")
    void shouldRejectBitsBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiInt(valueValue(), 7));
    }

    @Test
    @DisplayName("bits: rejects a value above the maximum (257)")
    @Tag("testId:evm.AbiInt/bits/rejects-above-maximum")
    void shouldRejectBitsAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiInt(valueValue(), 257));
    }

    @Test
    @DisplayName("bits: rejects a value that is no multiple (9)")
    @Tag("testId:evm.AbiInt/bits/rejects-not-amultiple")
    void shouldRejectBitsNotAMultiple() {
        assertThrows(IllegalArgumentException.class, () -> new AbiInt(valueValue(), 9));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiInt/value-semantics")
    void shouldCompareByValue() {
        final AbiInt subject = create();
        final AbiInt other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiInt/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    private static BigInteger valueValue() {
        return new BigInteger("1");
    }

    private static int bitsValue() {
        return 8;
    }

    private static AbiInt create() {
        return new AbiInt(valueValue(), bitsValue());
    }
}
