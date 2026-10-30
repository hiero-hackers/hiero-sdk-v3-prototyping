package org.hiero.mirrornode.nft;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
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
        final long serialNumber = serialNumberValue();
        final @Nullable AccountId accountId = accountIdValue();
        final byte @Nullable [] metadata = metadataValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable ZonedDateTime modifiedTimestamp = modifiedTimestampValue();
        final @Nullable AccountId delegatingSpender = delegatingSpenderValue();
        final @Nullable AccountId spender = spenderValue();
        final boolean deleted = deletedValue();
        final Nft subject = new Nft(tokenId, serialNumber, accountId, metadata, createdTimestamp, modifiedTimestamp, delegatingSpender, spender, deleted);
        assertValue(tokenId, subject.tokenId());
        assertValue(serialNumber, subject.serialNumber());
        assertValue(accountId, subject.accountId());
        assertValue(metadata, subject.metadata());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(modifiedTimestamp, subject.modifiedTimestamp());
        assertValue(delegatingSpender, subject.delegatingSpender());
        assertValue(spender, subject.spender());
        assertValue(deleted, subject.deleted());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.nft.Nft/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address tokenId = tokenIdValue();
        final long serialNumber = serialNumberValue();
        final @Nullable AccountId accountId = accountIdValue();
        final byte @Nullable [] metadata = metadataValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable ZonedDateTime modifiedTimestamp = modifiedTimestampValue();
        final @Nullable AccountId delegatingSpender = delegatingSpenderValue();
        final @Nullable AccountId spender = spenderValue();
        final boolean deleted = deletedValue();
        final Nft subject = Nft.builder()
                .tokenId(tokenId)
                .serialNumber(serialNumber)
                .accountId(accountId)
                .metadata(metadata)
                .createdTimestamp(createdTimestamp)
                .modifiedTimestamp(modifiedTimestamp)
                .delegatingSpender(delegatingSpender)
                .spender(spender)
                .deleted(deleted)
                .build();
        assertValue(tokenId, subject.tokenId());
        assertValue(serialNumber, subject.serialNumber());
        assertValue(accountId, subject.accountId());
        assertValue(metadata, subject.metadata());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(modifiedTimestamp, subject.modifiedTimestamp());
        assertValue(delegatingSpender, subject.delegatingSpender());
        assertValue(spender, subject.spender());
        assertValue(deleted, subject.deleted());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.nft.Nft/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final Nft subject = Nft.builder()
                .tokenId(tokenIdValue())
                .serialNumber(serialNumberValue())
                .deleted(deletedValue())
                .build();
        assertNull(subject.accountId());
        assertNull(subject.metadata());
        assertNull(subject.createdTimestamp());
        assertNull(subject.modifiedTimestamp());
        assertNull(subject.delegatingSpender());
        assertNull(subject.spender());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.nft.Nft/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> Nft.builder().build());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.nft.Nft/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new Nft((Address) null, serialNumberValue(), accountIdValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), delegatingSpenderValue(), spenderValue(), deletedValue()));
    }

    @Test
    @DisplayName("serialNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.nft.Nft/serialNumber/accepts-at-minimum")
    void shouldAcceptSerialNumberAtMinimum() {
        final long value = -9223372036854775808L;
        final Nft subject = new Nft(tokenIdValue(), value, accountIdValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), delegatingSpenderValue(), spenderValue(), deletedValue());
        assertValue(value, subject.serialNumber());
    }

    @Test
    @DisplayName("serialNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.nft.Nft/serialNumber/accepts-at-maximum")
    void shouldAcceptSerialNumberAtMaximum() {
        final long value = 9223372036854775807L;
        final Nft subject = new Nft(tokenIdValue(), value, accountIdValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), delegatingSpenderValue(), spenderValue(), deletedValue());
        assertValue(value, subject.serialNumber());
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
    @Tag("testId:mirrornode.nft.Nft/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final byte[] value = metadataValue();
        final byte[] expected = value.clone();
        final Nft subject = new Nft(tokenIdValue(), serialNumberValue(), accountIdValue(), value, createdTimestampValue(), modifiedTimestampValue(), delegatingSpenderValue(), spenderValue(), deletedValue());
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

    private static long serialNumberValue() {
        return 1L;
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static byte[] metadataValue() {
        return new byte[] {1, 2, 3};
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ZonedDateTime modifiedTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static AccountId delegatingSpenderValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId spenderValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static boolean deletedValue() {
        return true;
    }

    private static Nft create() {
        return new Nft(tokenIdValue(), serialNumberValue(), accountIdValue(), metadataValue(), createdTimestampValue(), modifiedTimestampValue(), delegatingSpenderValue(), spenderValue(), deletedValue());
    }
}
