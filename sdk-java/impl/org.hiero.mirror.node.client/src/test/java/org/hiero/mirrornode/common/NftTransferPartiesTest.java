package org.hiero.mirrornode.common;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NftTransferParties`, generated from the specs.
@DisplayName("NftTransferParties")
class NftTransferPartiesTest {

    @Test
    @DisplayName("creates a NftTransferParties and returns the values")
    @Tag("testId:mirrornode.common.NftTransferParties/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable AccountId senderAccountId = senderAccountIdValue();
        final @Nullable AccountId receiverAccountId = receiverAccountIdValue();
        final Address tokenId = tokenIdValue();
        final NftTransferParties subject = new NftTransferParties(senderAccountId, receiverAccountId, tokenId);
        assertValue(senderAccountId, subject.senderAccountId());
        assertValue(receiverAccountId, subject.receiverAccountId());
        assertValue(tokenId, subject.tokenId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.common.NftTransferParties/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable AccountId senderAccountId = senderAccountIdValue();
        final @Nullable AccountId receiverAccountId = receiverAccountIdValue();
        final Address tokenId = tokenIdValue();
        final NftTransferParties subject = NftTransferParties.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .tokenId(tokenId)
                .build();
        assertValue(senderAccountId, subject.senderAccountId());
        assertValue(receiverAccountId, subject.receiverAccountId());
        assertValue(tokenId, subject.tokenId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.common.NftTransferParties/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NftTransferParties subject = NftTransferParties.builder()
                .tokenId(tokenIdValue())
                .build();
        assertNull(subject.senderAccountId());
        assertNull(subject.receiverAccountId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.common.NftTransferParties/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NftTransferParties.builder().build());
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.common.NftTransferParties/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new NftTransferParties(senderAccountIdValue(), receiverAccountIdValue(), (Address) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.common.NftTransferParties/value-semantics")
    void shouldCompareByValue() {
        final NftTransferParties subject = create();
        final NftTransferParties other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId senderAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId receiverAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static NftTransferParties create() {
        return new NftTransferParties(senderAccountIdValue(), receiverAccountIdValue(), tokenIdValue());
    }
}
