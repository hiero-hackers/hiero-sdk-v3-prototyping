package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FeeExtra`, generated from the specs.
@DisplayName("FeeExtra")
class FeeExtraTest {

    @Test
    @DisplayName("creates a FeeExtra and returns the values")
    @Tag("testId:mirrornode.network.FeeExtra/create")
    void shouldCreateAndReturnTheValues() {
        final long charged = chargedValue();
        final long count = countValue();
        final long feePerUnit = feePerUnitValue();
        final long included = includedValue();
        final String name = nameValue();
        final long subtotal = subtotalValue();
        final FeeExtra subject = new FeeExtra(charged, count, feePerUnit, included, name, subtotal);
        assertValue(charged, subject.charged());
        assertValue(count, subject.count());
        assertValue(feePerUnit, subject.feePerUnit());
        assertValue(included, subject.included());
        assertValue(name, subject.name());
        assertValue(subtotal, subject.subtotal());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.network.FeeExtra/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long charged = chargedValue();
        final long count = countValue();
        final long feePerUnit = feePerUnitValue();
        final long included = includedValue();
        final String name = nameValue();
        final long subtotal = subtotalValue();
        final FeeExtra subject = FeeExtra.builder()
                .charged(charged)
                .count(count)
                .feePerUnit(feePerUnit)
                .included(included)
                .name(name)
                .subtotal(subtotal)
                .build();
        assertValue(charged, subject.charged());
        assertValue(count, subject.count());
        assertValue(feePerUnit, subject.feePerUnit());
        assertValue(included, subject.included());
        assertValue(name, subject.name());
        assertValue(subtotal, subject.subtotal());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.network.FeeExtra/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final FeeExtra subject = FeeExtra.builder()
                .charged(chargedValue())
                .count(countValue())
                .feePerUnit(feePerUnitValue())
                .included(includedValue())
                .name(nameValue())
                .subtotal(subtotalValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.network.FeeExtra/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> FeeExtra.builder().build());
    }

    @Test
    @DisplayName("charged: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeExtra/charged/accepts-at-minimum")
    void shouldAcceptChargedAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeExtra subject = new FeeExtra(value, countValue(), feePerUnitValue(), includedValue(), nameValue(), subtotalValue());
        assertValue(value, subject.charged());
    }

    @Test
    @DisplayName("charged: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeExtra/charged/accepts-at-maximum")
    void shouldAcceptChargedAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeExtra subject = new FeeExtra(value, countValue(), feePerUnitValue(), includedValue(), nameValue(), subtotalValue());
        assertValue(value, subject.charged());
    }

    @Test
    @DisplayName("count: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeExtra/count/accepts-at-minimum")
    void shouldAcceptCountAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeExtra subject = new FeeExtra(chargedValue(), value, feePerUnitValue(), includedValue(), nameValue(), subtotalValue());
        assertValue(value, subject.count());
    }

    @Test
    @DisplayName("count: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeExtra/count/accepts-at-maximum")
    void shouldAcceptCountAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeExtra subject = new FeeExtra(chargedValue(), value, feePerUnitValue(), includedValue(), nameValue(), subtotalValue());
        assertValue(value, subject.count());
    }

    @Test
    @DisplayName("feePerUnit: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeExtra/feePerUnit/accepts-at-minimum")
    void shouldAcceptFeePerUnitAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeExtra subject = new FeeExtra(chargedValue(), countValue(), value, includedValue(), nameValue(), subtotalValue());
        assertValue(value, subject.feePerUnit());
    }

    @Test
    @DisplayName("feePerUnit: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeExtra/feePerUnit/accepts-at-maximum")
    void shouldAcceptFeePerUnitAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeExtra subject = new FeeExtra(chargedValue(), countValue(), value, includedValue(), nameValue(), subtotalValue());
        assertValue(value, subject.feePerUnit());
    }

    @Test
    @DisplayName("included: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeExtra/included/accepts-at-minimum")
    void shouldAcceptIncludedAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeExtra subject = new FeeExtra(chargedValue(), countValue(), feePerUnitValue(), value, nameValue(), subtotalValue());
        assertValue(value, subject.included());
    }

    @Test
    @DisplayName("included: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeExtra/included/accepts-at-maximum")
    void shouldAcceptIncludedAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeExtra subject = new FeeExtra(chargedValue(), countValue(), feePerUnitValue(), value, nameValue(), subtotalValue());
        assertValue(value, subject.included());
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:mirrornode.network.FeeExtra/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new FeeExtra(chargedValue(), countValue(), feePerUnitValue(), includedValue(), (String) null, subtotalValue()));
    }

    @Test
    @DisplayName("subtotal: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeExtra/subtotal/accepts-at-minimum")
    void shouldAcceptSubtotalAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeExtra subject = new FeeExtra(chargedValue(), countValue(), feePerUnitValue(), includedValue(), nameValue(), value);
        assertValue(value, subject.subtotal());
    }

    @Test
    @DisplayName("subtotal: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeExtra/subtotal/accepts-at-maximum")
    void shouldAcceptSubtotalAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeExtra subject = new FeeExtra(chargedValue(), countValue(), feePerUnitValue(), includedValue(), nameValue(), value);
        assertValue(value, subject.subtotal());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.FeeExtra/value-semantics")
    void shouldCompareByValue() {
        final FeeExtra subject = create();
        final FeeExtra other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long chargedValue() {
        return 1L;
    }

    private static long countValue() {
        return 1L;
    }

    private static long feePerUnitValue() {
        return 1L;
    }

    private static long includedValue() {
        return 1L;
    }

    private static String nameValue() {
        return "value";
    }

    private static long subtotalValue() {
        return 1L;
    }

    private static FeeExtra create() {
        return new FeeExtra(chargedValue(), countValue(), feePerUnitValue(), includedValue(), nameValue(), subtotalValue());
    }
}
