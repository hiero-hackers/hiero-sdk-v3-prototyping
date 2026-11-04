package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiArrayType`, generated from the specs.
@DisplayName("AbiArrayType")
class AbiArrayTypeTest {

    @Test
    @DisplayName("creates an AbiArrayType and returns the values")
    @Tag("testId:evm.AbiArrayType/create")
    void shouldCreateAndReturnTheValues() {
        final AbiType elementType = elementTypeValue();
        final @Nullable Long length = lengthValue();
        final AbiArrayType subject = new AbiArrayType(elementType, length);
        assertValue(elementType, subject.elementType());
        assertValue(length, subject.length());
    }

    @Test
    @DisplayName("rejects null for elementType")
    @Tag("testId:evm.AbiArrayType/elementType/rejects-null")
    void shouldRejectNullElementType() {
        assertThrows(NullPointerException.class, () -> new AbiArrayType((AbiType) null, lengthValue()));
    }

    @Test
    @DisplayName("length: accepts the minimum (1)")
    @Tag("testId:evm.AbiArrayType/length/accepts-at-minimum")
    void shouldAcceptLengthAtMinimum() {
        final @Nullable Long value = 1L;
        final AbiArrayType subject = new AbiArrayType(elementTypeValue(), value);
        assertValue(value, subject.length());
    }

    @Test
    @DisplayName("length: accepts the maximum (4294967295)")
    @Tag("testId:evm.AbiArrayType/length/accepts-at-maximum")
    void shouldAcceptLengthAtMaximum() {
        final @Nullable Long value = 4294967295L;
        final AbiArrayType subject = new AbiArrayType(elementTypeValue(), value);
        assertValue(value, subject.length());
    }

    @Test
    @DisplayName("length: rejects a value below the minimum (0)")
    @Tag("testId:evm.AbiArrayType/length/rejects-below-minimum")
    void shouldRejectLengthBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiArrayType(elementTypeValue(), 0L));
    }

    @Test
    @DisplayName("length: rejects a value above the maximum (4294967296)")
    @Tag("testId:evm.AbiArrayType/length/rejects-above-maximum")
    void shouldRejectLengthAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new AbiArrayType(elementTypeValue(), 4294967296L));
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiArrayType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    private static AbiType elementTypeValue() {
        return new AbiUintType(256);
    }

    private static long lengthValue() {
        return 1L;
    }

    private static AbiArrayType create() {
        return new AbiArrayType(elementTypeValue(), lengthValue());
    }
}
