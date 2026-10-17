package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NftAllowance`, generated from the specs.
@DisplayName("NftAllowance")
class NftAllowanceTest {

    @Test
    @DisplayName("creates a NftAllowance and returns the values")
    @Tag("testId:mirrornode.account.NftAllowance/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId owner = ownerValue();
        final AccountId spender = spenderValue();
        final Address tokenId = tokenIdValue();
        final boolean approvedForAll = approvedForAllValue();
        final ZonedDateTime timestamp = timestampValue();
        final NftAllowance subject = new NftAllowance(owner, spender, tokenId, approvedForAll, timestamp);
        assertValue(owner, subject.owner());
        assertValue(spender, subject.spender());
        assertValue(tokenId, subject.tokenId());
        assertValue(approvedForAll, subject.approvedForAll());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.account.NftAllowance/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountId owner = ownerValue();
        final AccountId spender = spenderValue();
        final Address tokenId = tokenIdValue();
        final boolean approvedForAll = approvedForAllValue();
        final ZonedDateTime timestamp = timestampValue();
        final NftAllowance subject = NftAllowance.builder()
                .owner(owner)
                .spender(spender)
                .tokenId(tokenId)
                .approvedForAll(approvedForAll)
                .timestamp(timestamp)
                .build();
        assertValue(owner, subject.owner());
        assertValue(spender, subject.spender());
        assertValue(tokenId, subject.tokenId());
        assertValue(approvedForAll, subject.approvedForAll());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.account.NftAllowance/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NftAllowance subject = NftAllowance.builder()
                .owner(ownerValue())
                .spender(spenderValue())
                .tokenId(tokenIdValue())
                .approvedForAll(approvedForAllValue())
                .timestamp(timestampValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.account.NftAllowance/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NftAllowance.builder().build());
    }

    @Test
    @DisplayName("rejects null for owner")
    @Tag("testId:mirrornode.account.NftAllowance/owner/rejects-null")
    void shouldRejectNullOwner() {
        assertThrows(NullPointerException.class, () -> new NftAllowance((AccountId) null, spenderValue(), tokenIdValue(), approvedForAllValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for spender")
    @Tag("testId:mirrornode.account.NftAllowance/spender/rejects-null")
    void shouldRejectNullSpender() {
        assertThrows(NullPointerException.class, () -> new NftAllowance(ownerValue(), (AccountId) null, tokenIdValue(), approvedForAllValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.account.NftAllowance/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new NftAllowance(ownerValue(), spenderValue(), (Address) null, approvedForAllValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.account.NftAllowance/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new NftAllowance(ownerValue(), spenderValue(), tokenIdValue(), approvedForAllValue(), (ZonedDateTime) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.account.NftAllowance/value-semantics")
    void shouldCompareByValue() {
        final NftAllowance subject = create();
        final NftAllowance other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId ownerValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId spenderValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static boolean approvedForAllValue() {
        return true;
    }

    private static ZonedDateTime timestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static NftAllowance create() {
        return new NftAllowance(ownerValue(), spenderValue(), tokenIdValue(), approvedForAllValue(), timestampValue());
    }
}
