package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.TimestampRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TokenAllowance`, generated from the specs.
@DisplayName("TokenAllowance")
class TokenAllowanceTest {

    @Test
    @DisplayName("creates a TokenAllowance and returns the values")
    @Tag("testId:mirrornode.account.TokenAllowance/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId owner = ownerValue();
        final AccountId spender = spenderValue();
        final Address tokenId = tokenIdValue();
        final long amount = amountValue();
        final long amountGranted = amountGrantedValue();
        final TimestampRange timestamp = timestampValue();
        final TokenAllowance subject = new TokenAllowance(owner, spender, tokenId, amount, amountGranted, timestamp);
        assertValue(owner, subject.owner());
        assertValue(spender, subject.spender());
        assertValue(tokenId, subject.tokenId());
        assertValue(amount, subject.amount());
        assertValue(amountGranted, subject.amountGranted());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.account.TokenAllowance/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountId owner = ownerValue();
        final AccountId spender = spenderValue();
        final Address tokenId = tokenIdValue();
        final long amount = amountValue();
        final long amountGranted = amountGrantedValue();
        final TimestampRange timestamp = timestampValue();
        final TokenAllowance subject = TokenAllowance.builder()
                .owner(owner)
                .spender(spender)
                .tokenId(tokenId)
                .amount(amount)
                .amountGranted(amountGranted)
                .timestamp(timestamp)
                .build();
        assertValue(owner, subject.owner());
        assertValue(spender, subject.spender());
        assertValue(tokenId, subject.tokenId());
        assertValue(amount, subject.amount());
        assertValue(amountGranted, subject.amountGranted());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.account.TokenAllowance/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenAllowance subject = TokenAllowance.builder()
                .owner(ownerValue())
                .spender(spenderValue())
                .tokenId(tokenIdValue())
                .amount(amountValue())
                .amountGranted(amountGrantedValue())
                .timestamp(timestampValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.account.TokenAllowance/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenAllowance.builder().build());
    }

    @Test
    @DisplayName("rejects null for owner")
    @Tag("testId:mirrornode.account.TokenAllowance/owner/rejects-null")
    void shouldRejectNullOwner() {
        assertThrows(NullPointerException.class, () -> new TokenAllowance((AccountId) null, spenderValue(), tokenIdValue(), amountValue(), amountGrantedValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for spender")
    @Tag("testId:mirrornode.account.TokenAllowance/spender/rejects-null")
    void shouldRejectNullSpender() {
        assertThrows(NullPointerException.class, () -> new TokenAllowance(ownerValue(), (AccountId) null, tokenIdValue(), amountValue(), amountGrantedValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.account.TokenAllowance/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenAllowance(ownerValue(), spenderValue(), (Address) null, amountValue(), amountGrantedValue(), timestampValue()));
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.TokenAllowance/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenAllowance subject = new TokenAllowance(ownerValue(), spenderValue(), tokenIdValue(), value, amountGrantedValue(), timestampValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.TokenAllowance/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenAllowance subject = new TokenAllowance(ownerValue(), spenderValue(), tokenIdValue(), value, amountGrantedValue(), timestampValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amountGranted: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.TokenAllowance/amountGranted/accepts-at-minimum")
    void shouldAcceptAmountGrantedAtMinimum() {
        final long value = -9223372036854775808L;
        final TokenAllowance subject = new TokenAllowance(ownerValue(), spenderValue(), tokenIdValue(), amountValue(), value, timestampValue());
        assertValue(value, subject.amountGranted());
    }

    @Test
    @DisplayName("amountGranted: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.TokenAllowance/amountGranted/accepts-at-maximum")
    void shouldAcceptAmountGrantedAtMaximum() {
        final long value = 9223372036854775807L;
        final TokenAllowance subject = new TokenAllowance(ownerValue(), spenderValue(), tokenIdValue(), amountValue(), value, timestampValue());
        assertValue(value, subject.amountGranted());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.account.TokenAllowance/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new TokenAllowance(ownerValue(), spenderValue(), tokenIdValue(), amountValue(), amountGrantedValue(), (TimestampRange) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.account.TokenAllowance/value-semantics")
    void shouldCompareByValue() {
        final TokenAllowance subject = create();
        final TokenAllowance other = create();
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

    private static long amountValue() {
        return 1L;
    }

    private static long amountGrantedValue() {
        return 1L;
    }

    private static TimestampRange timestampValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static TokenAllowance create() {
        return new TokenAllowance(ownerValue(), spenderValue(), tokenIdValue(), amountValue(), amountGrantedValue(), timestampValue());
    }
}
