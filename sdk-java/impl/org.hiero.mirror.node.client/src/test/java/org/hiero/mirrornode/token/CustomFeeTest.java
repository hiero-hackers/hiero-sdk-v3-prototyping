package org.hiero.mirrornode.token;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.mirrornode.common.FixedFee;
import org.jspecify.annotations.Nullable;
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
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final CustomFee subject = new CustomFee(fixedFees, fractionalFees, royaltyFees, createdTimestamp);
        assertValue(fixedFees, subject.fixedFees());
        assertValue(fractionalFees, subject.fractionalFees());
        assertValue(royaltyFees, subject.royaltyFees());
        assertValue(createdTimestamp, subject.createdTimestamp());
    }

    @Test
    @DisplayName("creates a CustomFee with the default values")
    @Tag("testId:mirrornode.token.CustomFee/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final CustomFee subject = new CustomFee(createdTimestampValue());
        assertValue(List.of(), subject.fixedFees());
        assertValue(List.of(), subject.fractionalFees());
        assertValue(List.of(), subject.royaltyFees());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.token.CustomFee/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final List<FixedFee> fixedFees = fixedFeesValue();
        final List<FractionalFee> fractionalFees = fractionalFeesValue();
        final List<RoyaltyFee> royaltyFees = royaltyFeesValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final CustomFee subject = CustomFee.builder()
                .fixedFees(fixedFees)
                .fractionalFees(fractionalFees)
                .royaltyFees(royaltyFees)
                .createdTimestamp(createdTimestamp)
                .build();
        assertValue(fixedFees, subject.fixedFees());
        assertValue(fractionalFees, subject.fractionalFees());
        assertValue(royaltyFees, subject.royaltyFees());
        assertValue(createdTimestamp, subject.createdTimestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.token.CustomFee/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final CustomFee subject = CustomFee.builder()
                .build();
        assertValue(List.of(), subject.fixedFees());
        assertValue(List.of(), subject.fractionalFees());
        assertValue(List.of(), subject.royaltyFees());
        assertNull(subject.createdTimestamp());
    }

    @Test
    @DisplayName("rejects null for fixedFees")
    @Tag("testId:mirrornode.token.CustomFee/fixedFees/rejects-null")
    void shouldRejectNullFixedFees() {
        assertThrows(NullPointerException.class, () -> new CustomFee((List<FixedFee>) null, fractionalFeesValue(), royaltyFeesValue(), createdTimestampValue()));
    }

    @Test
    @DisplayName("the constructor copies fixedFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.token.CustomFee/fixedFees/copies-in")
    void shouldCopyFixedFeesIn() {
        final List<FixedFee> value = new ArrayList<>(fixedFeesValue());
        final List<FixedFee> expected = List.copyOf(value);
        final CustomFee subject = new CustomFee(value, fractionalFeesValue(), royaltyFeesValue(), createdTimestampValue());
        value.clear();
        assertValue(expected, subject.fixedFees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.fixedFees()).clear());
    }

    @Test
    @DisplayName("rejects null for fractionalFees")
    @Tag("testId:mirrornode.token.CustomFee/fractionalFees/rejects-null")
    void shouldRejectNullFractionalFees() {
        assertThrows(NullPointerException.class, () -> new CustomFee(fixedFeesValue(), (List<FractionalFee>) null, royaltyFeesValue(), createdTimestampValue()));
    }

    @Test
    @DisplayName("the constructor copies fractionalFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.token.CustomFee/fractionalFees/copies-in")
    void shouldCopyFractionalFeesIn() {
        final List<FractionalFee> value = new ArrayList<>(fractionalFeesValue());
        final List<FractionalFee> expected = List.copyOf(value);
        final CustomFee subject = new CustomFee(fixedFeesValue(), value, royaltyFeesValue(), createdTimestampValue());
        value.clear();
        assertValue(expected, subject.fractionalFees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.fractionalFees()).clear());
    }

    @Test
    @DisplayName("rejects null for royaltyFees")
    @Tag("testId:mirrornode.token.CustomFee/royaltyFees/rejects-null")
    void shouldRejectNullRoyaltyFees() {
        assertThrows(NullPointerException.class, () -> new CustomFee(fixedFeesValue(), fractionalFeesValue(), (List<RoyaltyFee>) null, createdTimestampValue()));
    }

    @Test
    @DisplayName("the constructor copies royaltyFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.token.CustomFee/royaltyFees/copies-in")
    void shouldCopyRoyaltyFeesIn() {
        final List<RoyaltyFee> value = new ArrayList<>(royaltyFeesValue());
        final List<RoyaltyFee> expected = List.copyOf(value);
        final CustomFee subject = new CustomFee(fixedFeesValue(), fractionalFeesValue(), value, createdTimestampValue());
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
        return List.of(new FixedFee(true, 1L, null, null));
    }

    private static List<FractionalFee> fractionalFeesValue() {
        return List.of(new FractionalFee(1L, 1L, null, null, true, null, 1L, true));
    }

    private static List<RoyaltyFee> royaltyFeesValue() {
        return List.of(new RoyaltyFee(1L, 1L, null, null, null, true));
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static CustomFee create() {
        return new CustomFee(fixedFeesValue(), fractionalFeesValue(), royaltyFeesValue(), createdTimestampValue());
    }
}
