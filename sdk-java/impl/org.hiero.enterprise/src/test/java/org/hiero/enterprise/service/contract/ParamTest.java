package org.hiero.enterprise.service.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `Param`, generated from the specs.
@DisplayName("Param")
class ParamTest {

    @Test
    @DisplayName("creates a Param and returns the values")
    void shouldCreateAndReturnTheValues() {
        final String valueArgument = valueValue();
        final String nativeType = nativeTypeValue();
        final ParamSupplier<String> supplier = supplierValue();
        final Param<String, String> subject = new Param<>(valueArgument, nativeType, supplier);
        assertValue(valueArgument, subject.value());
        assertValue(nativeType, subject.nativeType());
        assertValue(supplier, subject.supplier());
    }

    @Test
    @DisplayName("rejects null for value")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new Param<>((String) null, nativeTypeValue(), supplierValue()));
    }

    @Test
    @DisplayName("rejects null for nativeType")
    void shouldRejectNullNativeType() {
        assertThrows(NullPointerException.class, () -> new Param<>(valueValue(), (String) null, supplierValue()));
    }

    @Test
    @DisplayName("rejects null for supplier")
    void shouldRejectNullSupplier() {
        assertThrows(NullPointerException.class, () -> new Param<>(valueValue(), nativeTypeValue(), (ParamSupplier<String>) null));
    }

    private static String valueValue() {
        return "value";
    }

    private static String nativeTypeValue() {
        return "value";
    }

    private static ParamSupplier<String> supplierValue() {
        return new ParamSupplier<>();
    }

    private static Param<String, String> create() {
        return new Param<>(valueValue(), nativeTypeValue(), supplierValue());
    }
}
