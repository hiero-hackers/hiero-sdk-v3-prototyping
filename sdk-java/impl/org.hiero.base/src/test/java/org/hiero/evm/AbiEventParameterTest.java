package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiEventParameter`, generated from the specs.
@DisplayName("AbiEventParameter")
class AbiEventParameterTest {

    @Test
    @DisplayName("creates an AbiEventParameter and returns the values")
    @Tag("testId:evm.AbiEventParameter/create")
    void shouldCreateAndReturnTheValues() {
        final AbiType abiType = abiTypeValue();
        final boolean indexed = indexedValue();
        final AbiEventParameter subject = new AbiEventParameter(abiType, indexed);
        assertValue(abiType, subject.abiType());
        assertValue(indexed, subject.indexed());
    }

    @Test
    @DisplayName("creates an AbiEventParameter with the default values")
    @Tag("testId:evm.AbiEventParameter/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AbiEventParameter subject = new AbiEventParameter(abiTypeValue());
        assertValue(false, subject.indexed());
    }

    @Test
    @DisplayName("rejects null for abiType")
    @Tag("testId:evm.AbiEventParameter/abiType/rejects-null")
    void shouldRejectNullAbiType() {
        assertThrows(NullPointerException.class, () -> new AbiEventParameter((AbiType) null, indexedValue()));
    }

    private static AbiType abiTypeValue() {
        return new AbiUintType(256);
    }

    private static boolean indexedValue() {
        return true;
    }

    private static AbiEventParameter create() {
        return new AbiEventParameter(abiTypeValue(), indexedValue());
    }
}
