package org.hiero.consensusnode.queries.tokens;

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

/// Tests of `TokenNftInfo`, generated from the specs.
@DisplayName("TokenNftInfo")
class TokenNftInfoTest {

    @Test
    @DisplayName("creates a TokenNftInfo and returns the values")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/create")
    void shouldCreateAndReturnTheValues() {
        final Address tokenId = tokenIdValue();
        final long serial = serialValue();
        final AccountId accountId = accountIdValue();
        final ZonedDateTime creationTime = creationTimeValue();
        final byte[] metadata = metadataValue();
        final @Nullable AccountId spenderId = spenderIdValue();
        final TokenNftInfo subject = new TokenNftInfo(tokenId, serial, accountId, creationTime, metadata, spenderId);
        assertValue(tokenId, subject.tokenId());
        assertValue(serial, subject.serial());
        assertValue(accountId, subject.accountId());
        assertValue(creationTime, subject.creationTime());
        assertValue(metadata, subject.metadata());
        assertValue(spenderId, subject.spenderId());
    }

    @Test
    @DisplayName("creates a TokenNftInfo with the default values")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TokenNftInfo subject = new TokenNftInfo(tokenIdValue(), serialValue(), accountIdValue(), creationTimeValue(), spenderIdValue());
        assertValue(new byte[0], subject.metadata());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address tokenId = tokenIdValue();
        final long serial = serialValue();
        final AccountId accountId = accountIdValue();
        final ZonedDateTime creationTime = creationTimeValue();
        final byte[] metadata = metadataValue();
        final @Nullable AccountId spenderId = spenderIdValue();
        final TokenNftInfo subject = TokenNftInfo.builder()
                .tokenId(tokenId)
                .serial(serial)
                .accountId(accountId)
                .creationTime(creationTime)
                .metadata(metadata)
                .spenderId(spenderId)
                .build();
        assertValue(tokenId, subject.tokenId());
        assertValue(serial, subject.serial());
        assertValue(accountId, subject.accountId());
        assertValue(creationTime, subject.creationTime());
        assertValue(metadata, subject.metadata());
        assertValue(spenderId, subject.spenderId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenNftInfo subject = TokenNftInfo.builder()
                .tokenId(tokenIdValue())
                .serial(serialValue())
                .accountId(accountIdValue())
                .creationTime(creationTimeValue())
                .build();
        assertValue(new byte[0], subject.metadata());
        assertNull(subject.spenderId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenNftInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenNftInfo((Address) null, serialValue(), accountIdValue(), creationTimeValue(), metadataValue(), spenderIdValue()));
    }

    @Test
    @DisplayName("serial: accepts the minimum (1)")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/serial/accepts-at-minimum")
    void shouldAcceptSerialAtMinimum() {
        final long value = 1L;
        final TokenNftInfo subject = new TokenNftInfo(tokenIdValue(), value, accountIdValue(), creationTimeValue(), metadataValue(), spenderIdValue());
        assertValue(value, subject.serial());
    }

    @Test
    @DisplayName("serial: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/serial/accepts-at-maximum")
    void shouldAcceptSerialAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenNftInfo subject = new TokenNftInfo(tokenIdValue(), value, accountIdValue(), creationTimeValue(), metadataValue(), spenderIdValue());
        assertValue(value, subject.serial());
    }

    @Test
    @DisplayName("serial: rejects a value below the minimum (0)")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/serial/rejects-below-minimum")
    void shouldRejectSerialBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new TokenNftInfo(tokenIdValue(), 0L, accountIdValue(), creationTimeValue(), metadataValue(), spenderIdValue()));
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new TokenNftInfo(tokenIdValue(), serialValue(), (AccountId) null, creationTimeValue(), metadataValue(), spenderIdValue()));
    }

    @Test
    @DisplayName("rejects null for creationTime")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/creationTime/rejects-null")
    void shouldRejectNullCreationTime() {
        assertThrows(NullPointerException.class, () -> new TokenNftInfo(tokenIdValue(), serialValue(), accountIdValue(), (ZonedDateTime) null, metadataValue(), spenderIdValue()));
    }

    @Test
    @DisplayName("rejects null for metadata")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/metadata/rejects-null")
    void shouldRejectNullMetadata() {
        assertThrows(NullPointerException.class, () -> new TokenNftInfo(tokenIdValue(), serialValue(), accountIdValue(), creationTimeValue(), (byte[]) null, spenderIdValue()));
    }

    @Test
    @DisplayName("the constructor copies metadata; the accessor returns a copy")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/metadata/copies-in")
    void shouldCopyMetadataIn() {
        final byte[] value = metadataValue();
        final byte[] expected = value.clone();
        final TokenNftInfo subject = new TokenNftInfo(tokenIdValue(), serialValue(), accountIdValue(), creationTimeValue(), value, spenderIdValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.metadata());
        final byte[] returned = subject.metadata();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.metadata());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.queries.tokens.TokenNftInfo/value-semantics")
    void shouldCompareByValue() {
        final TokenNftInfo subject = create();
        final TokenNftInfo other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long serialValue() {
        return 1L;
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static ZonedDateTime creationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static byte[] metadataValue() {
        return new byte[] {1, 2, 3};
    }

    private static AccountId spenderIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TokenNftInfo create() {
        return new TokenNftInfo(tokenIdValue(), serialValue(), accountIdValue(), creationTimeValue(), metadataValue(), spenderIdValue());
    }
}
