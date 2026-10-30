package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.mirrornode.common.TimestampRange;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TokenAirdrop`, generated from the specs.
@DisplayName("TokenAirdrop")
class TokenAirdropTest {

    @Test
    @DisplayName("creates a TokenAirdrop and returns the values")
    @Tag("testId:mirrornode.account.TokenAirdrop/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId senderId = senderIdValue();
        final AccountId receiverId = receiverIdValue();
        final Address tokenId = tokenIdValue();
        final @Nullable Long amount = amountValue();
        final @Nullable Long serialNumber = serialNumberValue();
        final TimestampRange timestamp = timestampValue();
        final TokenAirdrop subject = new TokenAirdrop(senderId, receiverId, tokenId, amount, serialNumber, timestamp);
        assertValue(senderId, subject.senderId());
        assertValue(receiverId, subject.receiverId());
        assertValue(tokenId, subject.tokenId());
        assertValue(amount, subject.amount());
        assertValue(serialNumber, subject.serialNumber());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.account.TokenAirdrop/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountId senderId = senderIdValue();
        final AccountId receiverId = receiverIdValue();
        final Address tokenId = tokenIdValue();
        final @Nullable Long amount = amountValue();
        final @Nullable Long serialNumber = serialNumberValue();
        final TimestampRange timestamp = timestampValue();
        final TokenAirdrop subject = TokenAirdrop.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .tokenId(tokenId)
                .amount(amount)
                .serialNumber(serialNumber)
                .timestamp(timestamp)
                .build();
        assertValue(senderId, subject.senderId());
        assertValue(receiverId, subject.receiverId());
        assertValue(tokenId, subject.tokenId());
        assertValue(amount, subject.amount());
        assertValue(serialNumber, subject.serialNumber());
        assertValue(timestamp, subject.timestamp());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.account.TokenAirdrop/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TokenAirdrop subject = TokenAirdrop.builder()
                .senderId(senderIdValue())
                .receiverId(receiverIdValue())
                .tokenId(tokenIdValue())
                .timestamp(timestampValue())
                .build();
        assertNull(subject.amount());
        assertNull(subject.serialNumber());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.account.TokenAirdrop/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TokenAirdrop.builder().build());
    }

    @Test
    @DisplayName("rejects null for senderId")
    @Tag("testId:mirrornode.account.TokenAirdrop/senderId/rejects-null")
    void shouldRejectNullSenderId() {
        assertThrows(NullPointerException.class, () -> new TokenAirdrop((AccountId) null, receiverIdValue(), tokenIdValue(), amountValue(), serialNumberValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for receiverId")
    @Tag("testId:mirrornode.account.TokenAirdrop/receiverId/rejects-null")
    void shouldRejectNullReceiverId() {
        assertThrows(NullPointerException.class, () -> new TokenAirdrop(senderIdValue(), (AccountId) null, tokenIdValue(), amountValue(), serialNumberValue(), timestampValue()));
    }

    @Test
    @DisplayName("rejects null for tokenId")
    @Tag("testId:mirrornode.account.TokenAirdrop/tokenId/rejects-null")
    void shouldRejectNullTokenId() {
        assertThrows(NullPointerException.class, () -> new TokenAirdrop(senderIdValue(), receiverIdValue(), (Address) null, amountValue(), serialNumberValue(), timestampValue()));
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.TokenAirdrop/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final TokenAirdrop subject = new TokenAirdrop(senderIdValue(), receiverIdValue(), tokenIdValue(), value, serialNumberValue(), timestampValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.TokenAirdrop/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TokenAirdrop subject = new TokenAirdrop(senderIdValue(), receiverIdValue(), tokenIdValue(), value, serialNumberValue(), timestampValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("serialNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.TokenAirdrop/serialNumber/accepts-at-minimum")
    void shouldAcceptSerialNumberAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final TokenAirdrop subject = new TokenAirdrop(senderIdValue(), receiverIdValue(), tokenIdValue(), amountValue(), value, timestampValue());
        assertValue(value, subject.serialNumber());
    }

    @Test
    @DisplayName("serialNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.TokenAirdrop/serialNumber/accepts-at-maximum")
    void shouldAcceptSerialNumberAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TokenAirdrop subject = new TokenAirdrop(senderIdValue(), receiverIdValue(), tokenIdValue(), amountValue(), value, timestampValue());
        assertValue(value, subject.serialNumber());
    }

    @Test
    @DisplayName("rejects null for timestamp")
    @Tag("testId:mirrornode.account.TokenAirdrop/timestamp/rejects-null")
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class, () -> new TokenAirdrop(senderIdValue(), receiverIdValue(), tokenIdValue(), amountValue(), serialNumberValue(), (TimestampRange) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.account.TokenAirdrop/value-semantics")
    void shouldCompareByValue() {
        final TokenAirdrop subject = create();
        final TokenAirdrop other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId senderIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId receiverIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long amountValue() {
        return 1L;
    }

    private static long serialNumberValue() {
        return 1L;
    }

    private static TimestampRange timestampValue() {
        return new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null);
    }

    private static TokenAirdrop create() {
        return new TokenAirdrop(senderIdValue(), receiverIdValue(), tokenIdValue(), amountValue(), serialNumberValue(), timestampValue());
    }
}
