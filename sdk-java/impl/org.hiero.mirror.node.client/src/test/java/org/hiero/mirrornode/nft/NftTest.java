package org.hiero.mirrornode.nft;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Nft`, generated from the specs.
@DisplayName("Nft")
class NftTest {

    @Test
    @DisplayName("creates a Nft and returns the values")
    @Tag("testId:mirrornode.nft.Nft/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final long serial = serialValue();
        final AccountId owner = ownerValue();
        final byte[] metadata = metadataValue();
        final Nft subject = new Nft(tokenId, serial, owner, metadata);
        assertValue(tokenId, subject.tokenId());
        assertValue(serial, subject.serial());
        assertValue(owner, subject.owner());
        assertValue(metadata, subject.metadata());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.nft.Nft/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new Nft((Address) null, serialValue(), ownerValue(), metadataValue()));
    }

    @Test
    @DisplayName("serial: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.nft.Nft/serial/accepts-at-minimum")
    void shouldAcceptSerialAtMinimum() {
        final long value = -9223372036854775808L;
        final Nft subject = new Nft(tokenIdValue(), value, ownerValue(), metadataValue());
        assertValue(value, subject.serial());
    }

    @Test
    @DisplayName("serial: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.nft.Nft/serial/accepts-at-maximum")
    void shouldAcceptSerialAtMaximum() {
        final long value = 9223372036854775807L;
        final Nft subject = new Nft(tokenIdValue(), value, ownerValue(), metadataValue());
        assertValue(value, subject.serial());
    }

    @Test
    @DisplayName("rejects null for owner")
    @Tag("testId:mirrornode.nft.Nft/owner/rejects-null")
    void shouldRejectNullOwner() {
        assertThrows(NullPointerException.class, () -> new Nft(tokenIdValue(), serialValue(), (AccountId) null, metadataValue()));
    }

    @Test
    @DisplayName("rejects null for metadata")
    @Tag("testId:mirrornode.nft.Nft/metadata/rejects-null")
    void shouldRejectNullMetadata() {
        assertThrows(NullPointerException.class, () -> new Nft(tokenIdValue(), serialValue(), ownerValue(), (byte[]) null));
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
    @Tag("testId:mirrornode.nft.Nft/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final byte[] value = metadataValue();
        final byte[] expected = value.clone();
        final Nft subject = new Nft(tokenIdValue(), serialValue(), ownerValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.metadata());
        final byte[] returned = subject.metadata();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.metadata());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.nft.Nft/value-semantics")
    void shouldCompareByValue() {
        final Nft subject = create();
        final Nft other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long serialValue() {
        return 1L;
    }

    private static AccountId ownerValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static byte[] metadataValue() {
        return new byte[] {1, 2, 3};
    }

    private static Nft create() {
        return new Nft(tokenIdValue(), serialValue(), ownerValue(), metadataValue());
    }
}
