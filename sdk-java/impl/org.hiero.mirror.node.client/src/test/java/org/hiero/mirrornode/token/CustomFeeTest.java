package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.mirrornode.common.FixedFee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `CustomFee`, generated from the specs.
@DisplayName("CustomFee")
class CustomFeeTest {

    @Test
    @DisplayName("creates a CustomFee and returns the values")
    @Tag("testId:mirrornode.token.CustomFee/create")
    void shouldCreateAndReturnTheValues() {
        final List<FixedFee> fixedFees = fixedFeesValue();
        final List<FractionalFee> fractionalFees = fractionalFeesValue();
        final List<RoyaltyFee> royaltyFees = royaltyFeesValue();
        final CustomFee subject = new CustomFee(fixedFees, fractionalFees, royaltyFees);
        assertValue(fixedFees, subject.fixedFees());
        assertValue(fractionalFees, subject.fractionalFees());
        assertValue(royaltyFees, subject.royaltyFees());
    }

    @Test
    @DisplayName("rejects null for fixedFees")
    @Tag("testId:mirrornode.token.CustomFee/fixedFees/rejects-null")
    void shouldRejectNullFixedFees() {
        assertThrows(NullPointerException.class, () -> new CustomFee((List<FixedFee>) null, fractionalFeesValue(), royaltyFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies fixedFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.token.CustomFee/fixedFees/copies-in")
    void shouldCopyFixedFeesIn() {
        final List<FixedFee> value = new ArrayList<>(fixedFeesValue());
        final List<FixedFee> expected = List.copyOf(value);
        final CustomFee subject = new CustomFee(value, fractionalFeesValue(), royaltyFeesValue());
        value.clear();
        assertValue(expected, subject.fixedFees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.fixedFees()).clear());
    }

    @Test
    @DisplayName("rejects null for fractionalFees")
    @Tag("testId:mirrornode.token.CustomFee/fractionalFees/rejects-null")
    void shouldRejectNullFractionalFees() {
        assertThrows(NullPointerException.class, () -> new CustomFee(fixedFeesValue(), (List<FractionalFee>) null, royaltyFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies fractionalFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.token.CustomFee/fractionalFees/copies-in")
    void shouldCopyFractionalFeesIn() {
        final List<FractionalFee> value = new ArrayList<>(fractionalFeesValue());
        final List<FractionalFee> expected = List.copyOf(value);
        final CustomFee subject = new CustomFee(fixedFeesValue(), value, royaltyFeesValue());
        value.clear();
        assertValue(expected, subject.fractionalFees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.fractionalFees()).clear());
    }

    @Test
    @DisplayName("rejects null for royaltyFees")
    @Tag("testId:mirrornode.token.CustomFee/royaltyFees/rejects-null")
    void shouldRejectNullRoyaltyFees() {
        assertThrows(NullPointerException.class, () -> new CustomFee(fixedFeesValue(), fractionalFeesValue(), (List<RoyaltyFee>) null));
    }

    @Test
    @DisplayName("the constructor copies royaltyFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.token.CustomFee/royaltyFees/copies-in")
    void shouldCopyRoyaltyFeesIn() {
        final List<RoyaltyFee> value = new ArrayList<>(royaltyFeesValue());
        final List<RoyaltyFee> expected = List.copyOf(value);
        final CustomFee subject = new CustomFee(fixedFeesValue(), fractionalFeesValue(), value);
        value.clear();
        assertValue(expected, subject.royaltyFees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.royaltyFees()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.token.CustomFee/value-semantics")
    void shouldCompareByValue() {
        final CustomFee subject = create();
        final CustomFee other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static List<FixedFee> fixedFeesValue() {
        return List.of(new FixedFee(1L, null, null));
    }

    private static List<FractionalFee> fractionalFeesValue() {
        return List.of(new FractionalFee(1L, 1L, null, null));
    }

    private static List<RoyaltyFee> royaltyFeesValue() {
        return List.of(new RoyaltyFee(1L, 1L, 1L, null, null));
    }

    private static CustomFee create() {
        return new CustomFee(fixedFeesValue(), fractionalFeesValue(), royaltyFeesValue());
    }
}
