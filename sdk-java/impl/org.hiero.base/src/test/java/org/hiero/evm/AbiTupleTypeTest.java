package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AbiTupleType`, generated from the specs.
@DisplayName("AbiTupleType")
class AbiTupleTypeTest {

    @Test
    @DisplayName("creates an AbiTupleType and returns the values")
    @Tag("testId:evm.AbiTupleType/create")
    void shouldCreateAndReturnTheValues() {
        final List<AbiType> components = componentsValue();
        final AbiTupleType subject = new AbiTupleType(components);
        assertValue(components, subject.components());
    }

    @Test
    @DisplayName("rejects null for components")
    @Tag("testId:evm.AbiTupleType/components/rejects-null")
    void shouldRejectNullComponents() {
        assertThrows(NullPointerException.class, () -> new AbiTupleType((List<AbiType>) null));
    }

    @Test
    @DisplayName("the constructor copies components; the accessor returns an unmodifiable collection")
    @Tag("testId:evm.AbiTupleType/components/copies-in")
    void shouldCopyComponentsIn() {
        final List<AbiType> value = new ArrayList<>(componentsValue());
        final List<AbiType> expected = List.copyOf(value);
        final AbiTupleType subject = new AbiTupleType(value);
        value.clear();
        assertValue(expected, subject.components());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.components()).clear());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:evm.AbiTupleType/toString/call")
    void shouldCallToString() {
        assertNotNull(create().toString());
    }

    @Test
    @DisplayName("decode(data) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:evm.AbiTupleType/decode/call")
    void shouldCallDecode() throws Exception {
        try {
            assertNotNull(create().decode(dataValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    private static List<AbiType> componentsValue() {
        return List.of(new AbiUintType(256));
    }

    private static AbiTupleType create() {
        return new AbiTupleType(componentsValue());
    }

    private static byte[] dataValue() {
        return new byte[] {1, 2, 3};
    }
}
