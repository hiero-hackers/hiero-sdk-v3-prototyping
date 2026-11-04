package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiTuple`, generated from the specs.
@DisplayName("AbiTuple")
class AbiTupleTest {

    @Test
    @DisplayName("creates an AbiTuple and returns the values")
    @Tag("testId:evm.AbiTuple/create")
    void shouldCreateAndReturnTheValues() {
        final List<AbiValue> values = valuesValue();
        final AbiTuple subject = new AbiTuple(values);
        assertValue(values, subject.values());
    }

    @Test
    @DisplayName("rejects null for values")
    @Tag("testId:evm.AbiTuple/values/rejects-null")
    void shouldRejectNullValues() {
        assertThrows(NullPointerException.class, () -> new AbiTuple((List<AbiValue>) null));
    }

    @Test
    @DisplayName("the constructor copies values; the accessor returns an unmodifiable collection")
    @Tag("testId:evm.AbiTuple/values/copies-in")
    void shouldCopyValuesIn() {
        final List<AbiValue> value = new ArrayList<>(valuesValue());
        final List<AbiValue> expected = List.copyOf(value);
        final AbiTuple subject = new AbiTuple(value);
        value.clear();
        assertValue(expected, subject.values());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.values()).clear());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiTuple/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    @Test
    @DisplayName("encode() can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiTuple/encode/call")
    void shouldCallEncode() throws Exception {
        try {
            assertNotNull(create().encode());
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static List<AbiValue> valuesValue() {
        return List.of(new AbiUint(new BigInteger("1"), 256));
    }

    private static AbiTuple create() {
        return new AbiTuple(valuesValue());
    }
}
