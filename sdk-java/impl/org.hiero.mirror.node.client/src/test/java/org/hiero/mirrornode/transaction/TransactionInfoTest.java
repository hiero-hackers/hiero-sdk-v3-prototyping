package org.hiero.mirrornode.transaction;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.common.NftTransfer;
import org.hiero.mirrornode.common.StakingRewardTransfer;
import org.hiero.mirrornode.common.TokenTransfer;
import org.hiero.mirrornode.common.Transfer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TransactionInfo`, generated from the specs.
@DisplayName("TransactionInfo")
class TransactionInfoTest {

    @Test
    @DisplayName("creates a TransactionInfo and returns the values")
    @Tag("testId:mirrornode.transaction.TransactionInfo/create")
    void shouldCreateAndReturnTheValues() {
        final TransactionId transactionId = transactionIdValue();
        final byte[] transactionHash = transactionHashValue();
        final long chargedTxFee = chargedTxFeeValue();
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final @Nullable String entityId = entityIdValue();
        final long maxFee = maxFeeValue();
        final byte[] memo = memoValue();
        final TransactionType name = nameValue();
        final int nonce = nonceValue();
        final @Nullable String node = nodeValue();
        final @Nullable ZonedDateTime parentConsensusTimestamp = parentConsensusTimestampValue();
        final TransactionResult result = resultValue();
        final boolean scheduled = scheduledValue();
        final Duration validDuration = validDurationValue();
        final ZonedDateTime validStartTimestamp = validStartTimestampValue();
        final List<Transfer> transfers = transfersValue();
        final List<TokenTransfer> tokenTransfers = tokenTransfersValue();
        final List<NftTransfer> nftTransfers = nftTransfersValue();
        final List<StakingRewardTransfer> stakingRewardTransfers = stakingRewardTransfersValue();
        final TransactionInfo subject = new TransactionInfo(transactionId, transactionHash, chargedTxFee, consensusTimestamp, entityId, maxFee, memo, name, nonce, node, parentConsensusTimestamp, result, scheduled, validDuration, validStartTimestamp, transfers, tokenTransfers, nftTransfers, stakingRewardTransfers);
        assertValue(transactionId, subject.transactionId());
        assertValue(transactionHash, subject.transactionHash());
        assertValue(chargedTxFee, subject.chargedTxFee());
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(entityId, subject.entityId());
        assertValue(maxFee, subject.maxFee());
        assertValue(memo, subject.memo());
        assertValue(name, subject.name());
        assertValue(nonce, subject.nonce());
        assertValue(node, subject.node());
        assertValue(parentConsensusTimestamp, subject.parentConsensusTimestamp());
        assertValue(result, subject.result());
        assertValue(scheduled, subject.scheduled());
        assertValue(validDuration, subject.validDuration());
        assertValue(validStartTimestamp, subject.validStartTimestamp());
        assertValue(transfers, subject.transfers());
        assertValue(tokenTransfers, subject.tokenTransfers());
        assertValue(nftTransfers, subject.nftTransfers());
        assertValue(stakingRewardTransfers, subject.stakingRewardTransfers());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.transaction.TransactionInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final byte[] transactionHash = transactionHashValue();
        final long chargedTxFee = chargedTxFeeValue();
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final @Nullable String entityId = entityIdValue();
        final long maxFee = maxFeeValue();
        final byte[] memo = memoValue();
        final TransactionType name = nameValue();
        final int nonce = nonceValue();
        final @Nullable String node = nodeValue();
        final @Nullable ZonedDateTime parentConsensusTimestamp = parentConsensusTimestampValue();
        final TransactionResult result = resultValue();
        final boolean scheduled = scheduledValue();
        final Duration validDuration = validDurationValue();
        final ZonedDateTime validStartTimestamp = validStartTimestampValue();
        final List<Transfer> transfers = transfersValue();
        final List<TokenTransfer> tokenTransfers = tokenTransfersValue();
        final List<NftTransfer> nftTransfers = nftTransfersValue();
        final List<StakingRewardTransfer> stakingRewardTransfers = stakingRewardTransfersValue();
        final TransactionInfo subject = TransactionInfo.builder()
                .transactionId(transactionId)
                .transactionHash(transactionHash)
                .chargedTxFee(chargedTxFee)
                .consensusTimestamp(consensusTimestamp)
                .entityId(entityId)
                .maxFee(maxFee)
                .memo(memo)
                .name(name)
                .nonce(nonce)
                .node(node)
                .parentConsensusTimestamp(parentConsensusTimestamp)
                .result(result)
                .scheduled(scheduled)
                .validDuration(validDuration)
                .validStartTimestamp(validStartTimestamp)
                .transfers(transfers)
                .tokenTransfers(tokenTransfers)
                .nftTransfers(nftTransfers)
                .stakingRewardTransfers(stakingRewardTransfers)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(transactionHash, subject.transactionHash());
        assertValue(chargedTxFee, subject.chargedTxFee());
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(entityId, subject.entityId());
        assertValue(maxFee, subject.maxFee());
        assertValue(memo, subject.memo());
        assertValue(name, subject.name());
        assertValue(nonce, subject.nonce());
        assertValue(node, subject.node());
        assertValue(parentConsensusTimestamp, subject.parentConsensusTimestamp());
        assertValue(result, subject.result());
        assertValue(scheduled, subject.scheduled());
        assertValue(validDuration, subject.validDuration());
        assertValue(validStartTimestamp, subject.validStartTimestamp());
        assertValue(transfers, subject.transfers());
        assertValue(tokenTransfers, subject.tokenTransfers());
        assertValue(nftTransfers, subject.nftTransfers());
        assertValue(stakingRewardTransfers, subject.stakingRewardTransfers());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.transaction.TransactionInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TransactionInfo subject = TransactionInfo.builder()
                .transactionId(transactionIdValue())
                .transactionHash(transactionHashValue())
                .chargedTxFee(chargedTxFeeValue())
                .consensusTimestamp(consensusTimestampValue())
                .maxFee(maxFeeValue())
                .memo(memoValue())
                .name(nameValue())
                .nonce(nonceValue())
                .result(resultValue())
                .scheduled(scheduledValue())
                .validDuration(validDurationValue())
                .validStartTimestamp(validStartTimestampValue())
                .transfers(transfersValue())
                .tokenTransfers(tokenTransfersValue())
                .nftTransfers(nftTransfersValue())
                .stakingRewardTransfers(stakingRewardTransfersValue())
                .build();
        assertNull(subject.entityId());
        assertNull(subject.node());
        assertNull(subject.parentConsensusTimestamp());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.transaction.TransactionInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TransactionInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for transactionId")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transactionId/rejects-null")
    void shouldRejectNullTransactionId() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo((TransactionId) null, transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("rejects null for transactionHash")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transactionHash/rejects-null")
    void shouldRejectNullTransactionHash() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), (byte[]) null, chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("the constructor copies transactionHash; the accessor returns a copy")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transactionHash/copies-in")
    void shouldCopyTransactionHashIn() {
        final byte[] value = transactionHashValue();
        final byte[] expected = value.clone();
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), value, chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.transactionHash());
        final byte[] returned = subject.transactionHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.transactionHash());
    }

    @Test
    @DisplayName("chargedTxFee: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/chargedTxFee/accepts-at-minimum")
    void shouldAcceptChargedTxFeeAtMinimum() {
        final long value = -9223372036854775808L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), value, consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        assertValue(value, subject.chargedTxFee());
    }

    @Test
    @DisplayName("chargedTxFee: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/chargedTxFee/accepts-at-maximum")
    void shouldAcceptChargedTxFeeAtMaximum() {
        final long value = 9223372036854775807L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), value, consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        assertValue(value, subject.chargedTxFee());
    }

    @Test
    @DisplayName("rejects null for consensusTimestamp")
    @Tag("testId:mirrornode.transaction.TransactionInfo/consensusTimestamp/rejects-null")
    void shouldRejectNullConsensusTimestamp() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), (ZonedDateTime) null, entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("maxFee: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/maxFee/accepts-at-minimum")
    void shouldAcceptMaxFeeAtMinimum() {
        final long value = -9223372036854775808L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), value, memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        assertValue(value, subject.maxFee());
    }

    @Test
    @DisplayName("maxFee: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/maxFee/accepts-at-maximum")
    void shouldAcceptMaxFeeAtMaximum() {
        final long value = 9223372036854775807L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), value, memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        assertValue(value, subject.maxFee());
    }

    @Test
    @DisplayName("rejects null for memo")
    @Tag("testId:mirrornode.transaction.TransactionInfo/memo/rejects-null")
    void shouldRejectNullMemo() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), (byte[]) null, nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("the constructor copies memo; the accessor returns a copy")
    @Tag("testId:mirrornode.transaction.TransactionInfo/memo/copies-in")
    void shouldCopyMemoIn() {
        final byte[] value = memoValue();
        final byte[] expected = value.clone();
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), value, nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.memo());
        final byte[] returned = subject.memo();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.memo());
    }

    @Test
    @DisplayName("rejects null for name")
    @Tag("testId:mirrornode.transaction.TransactionInfo/name/rejects-null")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), (TransactionType) null, nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("nonce: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/nonce/accepts-at-minimum")
    void shouldAcceptNonceAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), value, nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/nonce/accepts-at-maximum")
    void shouldAcceptNonceAtMaximum() {
        final int value = 2147483647;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), value, nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("rejects null for result")
    @Tag("testId:mirrornode.transaction.TransactionInfo/result/rejects-null")
    void shouldRejectNullResult() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), (TransactionResult) null, scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("rejects null for validDuration")
    @Tag("testId:mirrornode.transaction.TransactionInfo/validDuration/rejects-null")
    void shouldRejectNullValidDuration() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), (Duration) null, validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("rejects null for validStartTimestamp")
    @Tag("testId:mirrornode.transaction.TransactionInfo/validStartTimestamp/rejects-null")
    void shouldRejectNullValidStartTimestamp() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), (ZonedDateTime) null, transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("rejects null for transfers")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transfers/rejects-null")
    void shouldRejectNullTransfers() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), (List<Transfer>) null, tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("the constructor copies transfers; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transfers/copies-in")
    void shouldCopyTransfersIn() {
        final List<Transfer> value = new ArrayList<>(transfersValue());
        final List<Transfer> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), value, tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
        value.clear();
        assertValue(expected, subject.transfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.transfers()).clear());
    }

    @Test
    @DisplayName("rejects null for tokenTransfers")
    @Tag("testId:mirrornode.transaction.TransactionInfo/tokenTransfers/rejects-null")
    void shouldRejectNullTokenTransfers() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), (List<TokenTransfer>) null, nftTransfersValue(), stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("the constructor copies tokenTransfers; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/tokenTransfers/copies-in")
    void shouldCopyTokenTransfersIn() {
        final List<TokenTransfer> value = new ArrayList<>(tokenTransfersValue());
        final List<TokenTransfer> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), value, nftTransfersValue(), stakingRewardTransfersValue());
        value.clear();
        assertValue(expected, subject.tokenTransfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.tokenTransfers()).clear());
    }

    @Test
    @DisplayName("rejects null for nftTransfers")
    @Tag("testId:mirrornode.transaction.TransactionInfo/nftTransfers/rejects-null")
    void shouldRejectNullNftTransfers() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), (List<NftTransfer>) null, stakingRewardTransfersValue()));
    }

    @Test
    @DisplayName("the constructor copies nftTransfers; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/nftTransfers/copies-in")
    void shouldCopyNftTransfersIn() {
        final List<NftTransfer> value = new ArrayList<>(nftTransfersValue());
        final List<NftTransfer> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), value, stakingRewardTransfersValue());
        value.clear();
        assertValue(expected, subject.nftTransfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.nftTransfers()).clear());
    }

    @Test
    @DisplayName("rejects null for stakingRewardTransfers")
    @Tag("testId:mirrornode.transaction.TransactionInfo/stakingRewardTransfers/rejects-null")
    void shouldRejectNullStakingRewardTransfers() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), (List<StakingRewardTransfer>) null));
    }

    @Test
    @DisplayName("the constructor copies stakingRewardTransfers; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/stakingRewardTransfers/copies-in")
    void shouldCopyStakingRewardTransfersIn() {
        final List<StakingRewardTransfer> value = new ArrayList<>(stakingRewardTransfersValue());
        final List<StakingRewardTransfer> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), value);
        value.clear();
        assertValue(expected, subject.stakingRewardTransfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.stakingRewardTransfers()).clear());
    }

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static byte[] transactionHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static long chargedTxFeeValue() {
        return 1L;
    }

    private static ZonedDateTime consensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String entityIdValue() {
        return "value";
    }

    private static long maxFeeValue() {
        return 1L;
    }

    private static byte[] memoValue() {
        return new byte[] {1, 2, 3};
    }

    private static TransactionType nameValue() {
        return BasicTransactionType.ACCOUNT_CREATE;
    }

    private static int nonceValue() {
        return 1;
    }

    private static String nodeValue() {
        return "value";
    }

    private static ZonedDateTime parentConsensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static TransactionResult resultValue() {
        return TransactionResult.SUCCESS;
    }

    private static boolean scheduledValue() {
        return true;
    }

    private static Duration validDurationValue() {
        return Duration.ofSeconds(1L);
    }

    private static ZonedDateTime validStartTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static List<Transfer> transfersValue() {
        return List.of(new Transfer(new AccountId(0L, 0L, "", 1001L, null, null), 1L, true));
    }

    private static List<TokenTransfer> tokenTransfersValue() {
        return List.of(new TokenTransfer(new Address(0L, 0L, "", 1001L), new AccountId(0L, 0L, "", 1001L, null, null), 1L, true));
    }

    private static List<NftTransfer> nftTransfersValue() {
        return List.of(new NftTransfer(true, null, 1L));
    }

    private static List<StakingRewardTransfer> stakingRewardTransfersValue() {
        return List.of(new StakingRewardTransfer(new AccountId(0L, 0L, "", 1001L, null, null), 1L));
    }

    private static TransactionInfo create() {
        return new TransactionInfo(transactionIdValue(), transactionHashValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue());
    }
}
