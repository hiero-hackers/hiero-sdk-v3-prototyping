package org.hiero.consensusnode.transactions.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `NftCollectionGrant`, generated from the specs.
@DisplayName("NftCollectionGrant")
class NftCollectionGrantTest {

    @Test
    @DisplayName("creates a NftCollectionGrant and returns the values")
    void shouldCreateAndReturnTheValues() {
        final boolean approved = approvedValue();
        final NftCollectionGrant subject = new NftCollectionGrant(approved);
        assertValue(approved, subject.approved());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final NftCollectionGrant subject = create();
        final NftCollectionGrant other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static boolean approvedValue() {
        return true;
    }

    private static NftCollectionGrant create() {
        return new NftCollectionGrant(approvedValue());
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
