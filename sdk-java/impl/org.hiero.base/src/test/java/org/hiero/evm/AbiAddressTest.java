package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiAddress`, generated from the specs.
@DisplayName("AbiAddress")
class AbiAddressTest {

    @Test
    @DisplayName("creates an AbiAddress and returns the values")
    @Tag("testId:evm.AbiAddress/create")
    void shouldCreateAndReturnTheValues() {
        final EvmAddress valueArgument = valueValue();
        final AbiAddress subject = new AbiAddress(valueArgument);
        assertValue(valueArgument, subject.value());
    }

    @Test
    @DisplayName("rejects null for value")
    @Tag("testId:evm.AbiAddress/value/rejects-null")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new AbiAddress((EvmAddress) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:evm.AbiAddress/value-semantics")
    void shouldCompareByValue() {
        final AbiAddress subject = create();
        final AbiAddress other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiAddress/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    private static EvmAddress valueValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static AbiAddress create() {
        return new AbiAddress(valueValue());
    }
}
