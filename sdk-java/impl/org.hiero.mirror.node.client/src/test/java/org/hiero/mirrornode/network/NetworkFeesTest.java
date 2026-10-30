package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NetworkFees`, generated from the specs.
@DisplayName("NetworkFees")
class NetworkFeesTest {

    @Test
    @DisplayName("creates a NetworkFees and returns the values")
    @Tag("testId:mirrornode.network.NetworkFees/create")
    void shouldCreateAndReturnTheValues() {
        final List<NetworkFee> fees = feesValue();
        final @Nullable ZonedDateTime timestamp = timestampValue();
        final NetworkFees subject = new NetworkFees(fees, timestamp);
        assertValue(fees, subject.fees());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("creates a NetworkFees with the default values")
    @Tag("testId:mirrornode.network.NetworkFees/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final NetworkFees subject = new NetworkFees(timestampValue());
        assertValue(List.of(), subject.fees());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.network.NetworkFees/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final List<NetworkFee> fees = feesValue();
        final @Nullable ZonedDateTime timestamp = timestampValue();
        final NetworkFees subject = NetworkFees.builder()
                .fees(fees)
                .timestamp(timestamp)
                .build();
        assertValue(fees, subject.fees());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.network.NetworkFees/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NetworkFees subject = NetworkFees.builder()
                .build();
        assertValue(List.of(), subject.fees());
        assertNull(subject.timestamp());
    }

    @Test
    @DisplayName("rejects null for fees")
    @Tag("testId:mirrornode.network.NetworkFees/fees/rejects-null")
    void shouldRejectNullFees() {
        assertThrows(NullPointerException.class, () -> new NetworkFees((List<NetworkFee>) null, timestampValue()));
    }

    @Test
    @DisplayName("the constructor copies fees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.network.NetworkFees/fees/copies-in")
    void shouldCopyFeesIn() {
        final List<NetworkFee> value = new ArrayList<>(feesValue());
        final List<NetworkFee> expected = List.copyOf(value);
        final NetworkFees subject = new NetworkFees(value, timestampValue());
        value.clear();
        assertValue(expected, subject.fees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.fees()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.network.NetworkFees/value-semantics")
    void shouldCompareByValue() {
        final NetworkFees subject = create();
        final NetworkFees other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static List<NetworkFee> feesValue() {
        return List.of(new NetworkFee(1L, "value"));
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static NetworkFees create() {
        return new NetworkFees(feesValue(), timestampValue());
    }
}
