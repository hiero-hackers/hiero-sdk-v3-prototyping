package org.hiero.consensusnode.transactions.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

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
}
