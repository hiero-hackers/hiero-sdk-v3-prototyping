package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TokenRelationship`, generated from the specs.
@DisplayName("TokenRelationship")
class TokenRelationshipTest {

    @Test
    @DisplayName("creates a TokenRelationship and returns the values")
    @Tag("testId:mirrornode.account.TokenRelationship/create")
    void shouldCreateAndReturnTheValues() {
        final @Nullable Boolean automaticAssociation = automaticAssociationValue();
        final long balance = balanceValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable Long decimals = decimalsValue();
        final @Nullable TokenFreezeStatus freezeStatus = freezeStatusValue();
        final TokenKycStatus kycStatus = kycStatusValue();
        final Address tokenId = tokenIdValue();
        final TokenRelationship subject = new TokenRelationship(automaticAssociation, balance, createdTimestamp, decimals, freezeStatus, kycStatus, tokenId);
        assertValue(automaticAssociation, subject.automaticAssociation());
        assertValue(balance, subject.balance());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(decimals, subject.decimals());
        assertValue(freezeStatus, subject.freezeStatus());
        assertValue(kycStatus, subject.kycStatus());
        assertValue(tokenId, subject.tokenId());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.account.TokenRelationship/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final @Nullable Boolean automaticAssociation = automaticAssociationValue();
        final long balance = balanceValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable Long decimals = decimalsValue();
        final @Nullable TokenFreezeStatus freezeStatus = freezeStatusValue();
        final TokenKycStatus kycStatus = kycStatusValue();
        final Address tokenId = tokenIdValue();
        final TokenRelationship subject = TokenRelationship.builder()
                .automaticAssociation(automaticAssociation)
                .balance(balance)
                .createdTimestamp(createdTimestamp)
                .decimals(decimals)
                .freezeStatus(freezeStatus)
                .kycStatus(kycStatus)
                .tokenId(tokenId)
                .build();
        assertValue(automaticAssociation, subject.automaticAssociation());
        assertValue(balance, subject.balance());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(decimals, subject.decimals());
        assertValue(freezeStatus, subject.freezeStatus());
        assertValue(kycStatus, subject.kycStatus());
        assertValue(tokenId, subject.tokenId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.account.TokenRelationship/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenRelationship subject = TokenRelationship.builder()
                .balance(balanceValue())
                .kycStatus(kycStatusValue())
                .tokenId(tokenIdValue())
                .build();
        assertNull(subject.automaticAssociation());
        assertNull(subject.createdTimestamp());
        assertNull(subject.decimals());
        assertNull(subject.freezeStatus());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.account.TokenRelationship/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenRelationship.builder().build());
    }

    @Test
    @DisplayName("balance: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.TokenRelationship/balance/accepts-at-minimum")
    void shouldAcceptBalanceAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenRelationship subject = new TokenRelationship(automaticAssociationValue(), value, createdTimestampValue(), decimalsValue(), freezeStatusValue(), kycStatusValue(), tokenIdValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("balance: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.TokenRelationship/balance/accepts-at-maximum")
    void shouldAcceptBalanceAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenRelationship subject = new TokenRelationship(automaticAssociationValue(), value, createdTimestampValue(), decimalsValue(), freezeStatusValue(), kycStatusValue(), tokenIdValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("decimals: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.TokenRelationship/decimals/accepts-at-minimum")
    void shouldAcceptDecimalsAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final TokenRelationship subject = new TokenRelationship(automaticAssociationValue(), balanceValue(), createdTimestampValue(), value, freezeStatusValue(), kycStatusValue(), tokenIdValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("decimals: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.TokenRelationship/decimals/accepts-at-maximum")
    void shouldAcceptDecimalsAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TokenRelationship subject = new TokenRelationship(automaticAssociationValue(), balanceValue(), createdTimestampValue(), value, freezeStatusValue(), kycStatusValue(), tokenIdValue());
        assertValue(value, subject.decimals());
    }

    @Test
    @DisplayName("rejects null for kycStatus")
    @Tag("testId:mirrornode.account.TokenRelationship/kycStatus/rejects-null")
    void shouldRejectNullKycStatus() {
        assertThrows(NullPointerException.class, () -> new TokenRelationship(automaticAssociationValue(), balanceValue(), createdTimestampValue(), decimalsValue(), freezeStatusValue(), (TokenKycStatus) null, tokenIdValue()));
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.account.TokenRelationship/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenRelationship(automaticAssociationValue(), balanceValue(), createdTimestampValue(), decimalsValue(), freezeStatusValue(), kycStatusValue(), (Address) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.account.TokenRelationship/value-semantics")
    void shouldCompareByValue() {
        final TokenRelationship subject = create();
        final TokenRelationship other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static boolean automaticAssociationValue() {
        return true;
    }

    private static long balanceValue() {
        return 1L;
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static long decimalsValue() {
        return 1L;
    }

    private static TokenFreezeStatus freezeStatusValue() {
        return TokenFreezeStatus.NOT_APPLICABLE;
    }

    private static TokenKycStatus kycStatusValue() {
        return TokenKycStatus.NOT_APPLICABLE;
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static TokenRelationship create() {
        return new TokenRelationship(automaticAssociationValue(), balanceValue(), createdTimestampValue(), decimalsValue(), freezeStatusValue(), kycStatusValue(), tokenIdValue());
    }
}
