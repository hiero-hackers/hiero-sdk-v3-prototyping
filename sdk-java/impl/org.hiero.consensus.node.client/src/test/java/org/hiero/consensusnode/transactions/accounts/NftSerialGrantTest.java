package org.hiero.consensusnode.transactions.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `NftSerialGrant`, generated from the specs.
@DisplayName("NftSerialGrant")
class NftSerialGrantTest {

    @Test
    @DisplayName("creates a NftSerialGrant and returns the values")
    void shouldCreateAndReturnTheValues() {
        final List<Long> serials = serialsValue();
        final NftSerialGrant subject = new NftSerialGrant(serials);
        assertValue(serials, subject.serials());
    }

    @Test
    @DisplayName("rejects null for serials")
    void shouldRejectNullSerials() {
        assertThrows(NullPointerException.class, () -> new NftSerialGrant((List<Long>) null));
    }

    @Test
    @DisplayName("serials: accepts the minimum number of elements")
    void shouldAcceptSerialsAtMinimumSize() {
        final List<Long> value = List.of(1L);
        final NftSerialGrant subject = new NftSerialGrant(value);
        assertValue(value, subject.serials());
    }

    @Test
    @DisplayName("serials: rejects fewer elements than the minimum")
    void shouldRejectSerialsBelowMinimumSize() {
        assertThrows(IllegalArgumentException.class, () -> new NftSerialGrant(List.of()));
    }

    @Test
    @DisplayName("the constructor copies serials; the accessor returns an unmodifiable collection")
    void shouldCopySerialsIn() {
        final List<Long> value = new ArrayList<>(serialsValue());
        final List<Long> expected = List.copyOf(value);
        final NftSerialGrant subject = new NftSerialGrant(value);
        value.clear();
        assertValue(expected, subject.serials());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.serials()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final NftSerialGrant subject = create();
        final NftSerialGrant other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static List<Long> serialsValue() {
        return List.of(1L);
    }

    private static NftSerialGrant create() {
        return new NftSerialGrant(serialsValue());
    }

    /// Asserts that two values are equal; arrays by their content.
    private static void assertValue(final @Nullable Object expected, final @Nullable Object actual) {
        if (expected instanceof byte[] bytes && actual instanceof byte[] other) {
            assertArrayEquals(bytes, other, () -> "expected " + Arrays.toString(bytes) + " but was " + Arrays.toString(other));
        } else {
            assertEquals(expected, actual);
        }
    }
}
