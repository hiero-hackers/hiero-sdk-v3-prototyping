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

/// Tests of `AbiArray`, generated from the specs.
@DisplayName("AbiArray")
class AbiArrayTest {

    @Test
    @DisplayName("creates an AbiArray and returns the values")
    @Tag("testId:evm.AbiArray/create")
    void shouldCreateAndReturnTheValues() {
        final AbiArrayType arrayType = arrayTypeValue();
        final List<AbiValue> values = valuesValue();
        final AbiArray subject = new AbiArray(arrayType, values);
        assertValue(arrayType, subject.arrayType());
        assertValue(values, subject.values());
    }

    @Test
    @DisplayName("rejects null for arrayType")
    @Tag("testId:evm.AbiArray/arrayType/rejects-null")
    void shouldRejectNullArrayType() {
        assertThrows(NullPointerException.class, () -> new AbiArray((AbiArrayType) null, valuesValue()));
    }

    @Test
    @DisplayName("rejects null for values")
    @Tag("testId:evm.AbiArray/values/rejects-null")
    void shouldRejectNullValues() {
        assertThrows(NullPointerException.class, () -> new AbiArray(arrayTypeValue(), (List<AbiValue>) null));
    }

    @Test
    @DisplayName("the constructor copies values; the accessor returns an unmodifiable collection")
    @Tag("testId:evm.AbiArray/values/copies-in")
    void shouldCopyValuesIn() {
        final List<AbiValue> value = new ArrayList<>(valuesValue());
        final List<AbiValue> expected = List.copyOf(value);
        final AbiArray subject = new AbiArray(arrayTypeValue(), value);
        value.clear();
        assertValue(expected, subject.values());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.values()).clear());
    }

    @Test
    @DisplayName("abiType() can be called and returns a value")
    @Tag("testId:evm.AbiArray/abiType/call")
    void shouldCallAbiType() {
        assertNotNull(create().abiType());
    }

    private static AbiArrayType arrayTypeValue() {
        return new AbiArrayType(new AbiUintType(256), null);
    }

    private static List<AbiValue> valuesValue() {
        return List.of(new AbiUint(new BigInteger("1"), 256));
    }

    private static AbiArray create() {
        return new AbiArray(arrayTypeValue(), valuesValue());
    }
}
