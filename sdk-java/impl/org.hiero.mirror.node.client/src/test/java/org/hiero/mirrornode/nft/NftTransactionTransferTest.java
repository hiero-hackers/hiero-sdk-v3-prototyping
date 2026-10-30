package org.hiero.mirrornode.nft;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.transaction.BasicTransactionType;
import org.hiero.mirrornode.transaction.TransactionType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `NftTransactionTransfer`, generated from the specs.
@DisplayName("NftTransactionTransfer")
class NftTransactionTransferTest {

    @Test
    @DisplayName("creates a NftTransactionTransfer and returns the values")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/create")
    void shouldCreateAndReturnTheValues() {
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final boolean isApproval = isApprovalValue();
        final int nonce = nonceValue();
        final @Nullable AccountId receiverAccountId = receiverAccountIdValue();
        final @Nullable AccountId senderAccountId = senderAccountIdValue();
        final TransactionId transactionId = transactionIdValue();
        final TransactionType type = typeValue();
        final NftTransactionTransfer subject = new NftTransactionTransfer(consensusTimestamp, isApproval, nonce, receiverAccountId, senderAccountId, transactionId, type);
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(isApproval, subject.isApproval());
        assertValue(nonce, subject.nonce());
        assertValue(receiverAccountId, subject.receiverAccountId());
        assertValue(senderAccountId, subject.senderAccountId());
        assertValue(transactionId, subject.transactionId());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final boolean isApproval = isApprovalValue();
        final int nonce = nonceValue();
        final @Nullable AccountId receiverAccountId = receiverAccountIdValue();
        final @Nullable AccountId senderAccountId = senderAccountIdValue();
        final TransactionId transactionId = transactionIdValue();
        final TransactionType type = typeValue();
        final NftTransactionTransfer subject = NftTransactionTransfer.builder()
                .consensusTimestamp(consensusTimestamp)
                .isApproval(isApproval)
                .nonce(nonce)
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .transactionId(transactionId)
                .type(type)
                .build();
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(isApproval, subject.isApproval());
        assertValue(nonce, subject.nonce());
        assertValue(receiverAccountId, subject.receiverAccountId());
        assertValue(senderAccountId, subject.senderAccountId());
        assertValue(transactionId, subject.transactionId());
        assertValue(type, subject.type());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final NftTransactionTransfer subject = NftTransactionTransfer.builder()
                .consensusTimestamp(consensusTimestampValue())
                .isApproval(isApprovalValue())
                .nonce(nonceValue())
                .transactionId(transactionIdValue())
                .type(typeValue())
                .build();
        assertNull(subject.receiverAccountId());
        assertNull(subject.senderAccountId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> NftTransactionTransfer.builder().build());
    }

    @Test
    @DisplayName("rejects null for consensusTimestamp")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/consensusTimestamp/rejects-null")
    void shouldRejectNullConsensusTimestamp() {
        assertThrows(NullPointerException.class, () -> new NftTransactionTransfer((ZonedDateTime) null, isApprovalValue(), nonceValue(), receiverAccountIdValue(), senderAccountIdValue(), transactionIdValue(), typeValue()));
    }

    @Test
    @DisplayName("nonce: accepts the minimum (0)")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/nonce/accepts-at-minimum")
    void shouldAcceptNonceAtMinimum() {
        final int value = 0;
        final NftTransactionTransfer subject = new NftTransactionTransfer(consensusTimestampValue(), isApprovalValue(), value, receiverAccountIdValue(), senderAccountIdValue(), transactionIdValue(), typeValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/nonce/accepts-at-maximum")
    void shouldAcceptNonceAtMaximum() {
        final int value = 2147483647;
        final NftTransactionTransfer subject = new NftTransactionTransfer(consensusTimestampValue(), isApprovalValue(), value, receiverAccountIdValue(), senderAccountIdValue(), transactionIdValue(), typeValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: rejects a value below the minimum (-1)")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/nonce/rejects-below-minimum")
    void shouldRejectNonceBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new NftTransactionTransfer(consensusTimestampValue(), isApprovalValue(), -1, receiverAccountIdValue(), senderAccountIdValue(), transactionIdValue(), typeValue()));
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new NftTransactionTransfer(consensusTimestampValue(), isApprovalValue(), nonceValue(), receiverAccountIdValue(), senderAccountIdValue(), (TransactionId) null, typeValue()));
    }

    @Test
    @DisplayName("rejects null for type")
    @Tag("testId:mirrornode.nft.NftTransactionTransfer/type/rejects-null")
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> new NftTransactionTransfer(consensusTimestampValue(), isApprovalValue(), nonceValue(), receiverAccountIdValue(), senderAccountIdValue(), transactionIdValue(), (TransactionType) null));
    }

    private static ZonedDateTime consensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static boolean isApprovalValue() {
        return true;
    }

    private static int nonceValue() {
        return 1;
    }

    private static AccountId receiverAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId senderAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static TransactionType typeValue() {
        return BasicTransactionType.ATOMIC_BATCH;
    }

    private static NftTransactionTransfer create() {
        return new NftTransactionTransfer(consensusTimestampValue(), isApprovalValue(), nonceValue(), receiverAccountIdValue(), senderAccountIdValue(), transactionIdValue(), typeValue());
    }
}
