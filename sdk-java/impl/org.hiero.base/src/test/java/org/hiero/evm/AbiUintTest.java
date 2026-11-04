package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiUint`, generated from the specs.
@DisplayName("AbiUint")
class AbiUintTest {

    @Test
    @DisplayName("creates an AbiUint and returns the values")
    @Tag("testId:evm.AbiUint/create")
    void shouldCreateAndReturnTheValues() {
        final BigInteger valueArgument = valueValue();
        final int bits = bitsValue();
        final AbiUint subject = new AbiUint(valueArgument, bits);
        assertValue(valueArgument, subject.value());
        assertValue(bits, subject.bits());
    }

    @Test
    @DisplayName("creates an AbiUint with the default values")
    @Tag("testId:evm.AbiUint/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AbiUint subject = new AbiUint(valueValue());
        assertValue(256, subject.bits());
    }

    @Test
    @DisplayName("rejects null for value")
    @Tag("testId:evm.AbiUint/value/rejects-null")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new AbiUint((BigInteger) null, bitsValue()));
    }

    @Test
    @DisplayName("value: accepts the minimum (0)")
    @Tag("testId:evm.AbiUint/value/accepts-at-minimum")
    void shouldAcceptValueAtMinimum() {
        final BigInteger value = new BigInteger("0");
        final AbiUint subject = new AbiUint(value, bitsValue());
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: accepts the maximum (115792089237316195423570985008687907853269984665640564039457584007913129639935)")
    @Tag("testId:evm.AbiUint/value/accepts-at-maximum")
    void shouldAcceptValueAtMaximum() {
        final BigInteger value = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935");
        final AbiUint subject = new AbiUint(value, bitsValue());
        assertValue(value, subject.value());
    }

    @Test
    @DisplayName("value: rejects a value below the minimum (-1)")
    @Tag("testId:evm.AbiUint/value/rejects-below-minimum")
    void shouldRejectValueBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiUint(new BigInteger("-1"), bitsValue()));
    }

    @Test
    @DisplayName("value: rejects a value above the maximum (115792089237316195423570985008687907853269984665640564039457584007913129639936)")
    @Tag("testId:evm.AbiUint/value/rejects-above-maximum")
    void shouldRejectValueAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiUint(new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639936"), bitsValue()));
    }

    @Test
    @DisplayName("bits: accepts the minimum (8)")
    @Tag("testId:evm.AbiUint/bits/accepts-at-minimum")
    void shouldAcceptBitsAtMinimum() {
        final int value = 8;
        final AbiUint subject = new AbiUint(valueValue(), value);
        assertValue(value, subject.bits());
    }

    @Test
    @DisplayName("bits: accepts the maximum (256)")
    @Tag("testId:evm.AbiUint/bits/accepts-at-maximum")
    void shouldAcceptBitsAtMaximum() {
        final int value = 256;
        final AbiUint subject = new AbiUint(valueValue(), value);
        assertValue(value, subject.bits());
    }

    @Test
    @DisplayName("bits: rejects a value below the minimum (7)")
    @Tag("testId:evm.AbiUint/bits/rejects-below-minimum")
    void shouldRejectBitsBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiUint(valueValue(), 7));
    }

    @Test
    @DisplayName("bits: rejects a value above the maximum (257)")
    @Tag("testId:evm.AbiUint/bits/rejects-above-maximum")
    void shouldRejectBitsAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiUint(valueValue(), 257));
    }

    @Test
    @DisplayName("bits: rejects a value that is no multiple (9)")
    @Tag("testId:evm.AbiUint/bits/rejects-not-amultiple")
    void shouldRejectBitsNotAMultiple() {
        assertThrows(IllegalArgumentException.class, () -> new AbiUint(valueValue(), 9));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiUint/value-semantics")
    void shouldCompareByValue() {
        final AbiUint subject = create();
        final AbiUint other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiUint/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    private static BigInteger valueValue() {
        return new BigInteger("1");
    }

    private static int bitsValue() {
        return 8;
    }

    private static AbiUint create() {
        return new AbiUint(valueValue(), bitsValue());
    }
}
