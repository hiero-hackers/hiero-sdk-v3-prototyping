package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FeeEstimateComponent`, generated from the specs.
@DisplayName("FeeEstimateComponent")
class FeeEstimateComponentTest {

    @Test
    @DisplayName("creates a FeeEstimateComponent and returns the values")
    @Tag("testId:mirrornode.network.FeeEstimateComponent/create")
    void shouldCreateAndReturnTheValues() {
        final long base = baseValue();
        final List<FeeExtra> extras = extrasValue();
        final FeeEstimateComponent subject = new FeeEstimateComponent(base, extras);
        assertValue(base, subject.base());
        assertValue(extras, subject.extras());
    }

    @Test
    @DisplayName("creates a FeeEstimateComponent with the default values")
    @Tag("testId:mirrornode.network.FeeEstimateComponent/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final FeeEstimateComponent subject = new FeeEstimateComponent(baseValue());
        assertValue(List.of(), subject.extras());
    }

    @Test
    @DisplayName("base: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.network.FeeEstimateComponent/base/accepts-at-minimum")
    void shouldAcceptBaseAtMinimum() {
        final long value = -9223372036854775808L;
        final FeeEstimateComponent subject = new FeeEstimateComponent(value, extrasValue());
        assertValue(value, subject.base());
    }

    @Test
    @DisplayName("base: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.network.FeeEstimateComponent/base/accepts-at-maximum")
    void shouldAcceptBaseAtMaximum() {
        final long value = 9223372036854775807L;
        final FeeEstimateComponent subject = new FeeEstimateComponent(value, extrasValue());
        assertValue(value, subject.base());
    }

    @Test
    @DisplayName("rejects null for extras")
    @Tag("testId:mirrornode.network.FeeEstimateComponent/extras/rejects-null")
    void shouldRejectNullExtras() {
        assertThrows(NullPointerException.class, () -> new FeeEstimateComponent(baseValue(), (List<FeeExtra>) null));
    }

    @Test
    @DisplayName("the constructor copies extras; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.network.FeeEstimateComponent/extras/copies-in")
    void shouldCopyExtrasIn() {
        final List<FeeExtra> value = new ArrayList<>(extrasValue());
        final List<FeeExtra> expected = List.copyOf(value);
        final FeeEstimateComponent subject = new FeeEstimateComponent(baseValue(), value);
        value.clear();
        assertValue(expected, subject.extras());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.extras()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.FeeEstimateComponent/value-semantics")
    void shouldCompareByValue() {
        final FeeEstimateComponent subject = create();
        final FeeEstimateComponent other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long baseValue() {
        return 1L;
    }

    private static List<FeeExtra> extrasValue() {
        return List.of(new FeeExtra(1L, 1L, 1L, 1L, "value", 1L));
    }

    private static FeeEstimateComponent create() {
        return new FeeEstimateComponent(baseValue(), extrasValue());
    }
}
