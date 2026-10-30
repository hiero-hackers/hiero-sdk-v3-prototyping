package org.hiero.mirrornode.transaction;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.hiero.mirrornode.common.NftTransfer;
import org.hiero.mirrornode.common.NftTransferParties;
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
        final @Nullable Authority batchKey = batchKeyValue();
        final byte @Nullable [] bytes = bytesValue();
        final long chargedTxFee = chargedTxFeeValue();
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final @Nullable Address entityId = entityIdValue();
        final @Nullable Boolean highVolume = highVolumeValue();
        final @Nullable Long highVolumePricingMultiplier = highVolumePricingMultiplierValue();
        final List<CustomFeeLimit> maxCustomFees = maxCustomFeesValue();
        final long maxFee = maxFeeValue();
        final byte[] memo = memoValue();
        final TransactionType name = nameValue();
        final int nonce = nonceValue();
        final @Nullable AccountId node = nodeValue();
        final @Nullable ZonedDateTime parentConsensusTimestamp = parentConsensusTimestampValue();
        final String result = resultValue();
        final boolean scheduled = scheduledValue();
        final Duration validDuration = validDurationValue();
        final ZonedDateTime validStartTimestamp = validStartTimestampValue();
        final List<Transfer> transfers = transfersValue();
        final List<TokenTransfer> tokenTransfers = tokenTransfersValue();
        final List<NftTransfer> nftTransfers = nftTransfersValue();
        final List<StakingRewardTransfer> stakingRewardTransfers = stakingRewardTransfersValue();
        final List<AssessedCustomFee> assessedCustomFees = assessedCustomFeesValue();
        final TransactionInfo subject = new TransactionInfo(transactionId, transactionHash, batchKey, bytes, chargedTxFee, consensusTimestamp, entityId, highVolume, highVolumePricingMultiplier, maxCustomFees, maxFee, memo, name, nonce, node, parentConsensusTimestamp, result, scheduled, validDuration, validStartTimestamp, transfers, tokenTransfers, nftTransfers, stakingRewardTransfers, assessedCustomFees);
        assertValue(transactionId, subject.transactionId());
        assertValue(transactionHash, subject.transactionHash());
        assertValue(batchKey, subject.batchKey());
        assertValue(bytes, subject.bytes());
        assertValue(chargedTxFee, subject.chargedTxFee());
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(entityId, subject.entityId());
        assertValue(highVolume, subject.highVolume());
        assertValue(highVolumePricingMultiplier, subject.highVolumePricingMultiplier());
        assertValue(maxCustomFees, subject.maxCustomFees());
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
        assertValue(assessedCustomFees, subject.assessedCustomFees());
    }

    @Test
    @DisplayName("creates a TransactionInfo with the default values")
    @Tag("testId:mirrornode.transaction.TransactionInfo/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue());
        assertValue(List.of(), subject.maxCustomFees());
        assertValue(List.of(), subject.transfers());
        assertValue(List.of(), subject.tokenTransfers());
        assertValue(List.of(), subject.nftTransfers());
        assertValue(List.of(), subject.stakingRewardTransfers());
        assertValue(List.of(), subject.assessedCustomFees());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.transaction.TransactionInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final TransactionId transactionId = transactionIdValue();
        final byte[] transactionHash = transactionHashValue();
        final @Nullable Authority batchKey = batchKeyValue();
        final byte @Nullable [] bytes = bytesValue();
        final long chargedTxFee = chargedTxFeeValue();
        final ZonedDateTime consensusTimestamp = consensusTimestampValue();
        final @Nullable Address entityId = entityIdValue();
        final @Nullable Boolean highVolume = highVolumeValue();
        final @Nullable Long highVolumePricingMultiplier = highVolumePricingMultiplierValue();
        final List<CustomFeeLimit> maxCustomFees = maxCustomFeesValue();
        final long maxFee = maxFeeValue();
        final byte[] memo = memoValue();
        final TransactionType name = nameValue();
        final int nonce = nonceValue();
        final @Nullable AccountId node = nodeValue();
        final @Nullable ZonedDateTime parentConsensusTimestamp = parentConsensusTimestampValue();
        final String result = resultValue();
        final boolean scheduled = scheduledValue();
        final Duration validDuration = validDurationValue();
        final ZonedDateTime validStartTimestamp = validStartTimestampValue();
        final List<Transfer> transfers = transfersValue();
        final List<TokenTransfer> tokenTransfers = tokenTransfersValue();
        final List<NftTransfer> nftTransfers = nftTransfersValue();
        final List<StakingRewardTransfer> stakingRewardTransfers = stakingRewardTransfersValue();
        final List<AssessedCustomFee> assessedCustomFees = assessedCustomFeesValue();
        final TransactionInfo subject = TransactionInfo.builder()
                .transactionId(transactionId)
                .transactionHash(transactionHash)
                .batchKey(batchKey)
                .bytes(bytes)
                .chargedTxFee(chargedTxFee)
                .consensusTimestamp(consensusTimestamp)
                .entityId(entityId)
                .highVolume(highVolume)
                .highVolumePricingMultiplier(highVolumePricingMultiplier)
                .maxCustomFees(maxCustomFees)
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
                .assessedCustomFees(assessedCustomFees)
                .build();
        assertValue(transactionId, subject.transactionId());
        assertValue(transactionHash, subject.transactionHash());
        assertValue(batchKey, subject.batchKey());
        assertValue(bytes, subject.bytes());
        assertValue(chargedTxFee, subject.chargedTxFee());
        assertValue(consensusTimestamp, subject.consensusTimestamp());
        assertValue(entityId, subject.entityId());
        assertValue(highVolume, subject.highVolume());
        assertValue(highVolumePricingMultiplier, subject.highVolumePricingMultiplier());
        assertValue(maxCustomFees, subject.maxCustomFees());
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
        assertValue(assessedCustomFees, subject.assessedCustomFees());
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
                .build();
        assertNull(subject.batchKey());
        assertNull(subject.bytes());
        assertNull(subject.entityId());
        assertNull(subject.highVolume());
        assertNull(subject.highVolumePricingMultiplier());
        assertValue(List.of(), subject.maxCustomFees());
        assertNull(subject.node());
        assertNull(subject.parentConsensusTimestamp());
        assertValue(List.of(), subject.transfers());
        assertValue(List.of(), subject.tokenTransfers());
        assertValue(List.of(), subject.nftTransfers());
        assertValue(List.of(), subject.stakingRewardTransfers());
        assertValue(List.of(), subject.assessedCustomFees());
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
        assertThrows(NullPointerException.class, () -> new TransactionInfo((TransactionId) null, transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("rejects null for transactionHash")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transactionHash/rejects-null")
    void shouldRejectNullTransactionHash() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), (byte[]) null, batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies transactionHash; the accessor returns a copy")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transactionHash/copies-in")
    void shouldCopyTransactionHashIn() {
        final byte[] value = transactionHashValue();
        final byte[] expected = value.clone();
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), value, batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.transactionHash());
        final byte[] returned = subject.transactionHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.transactionHash());
    }

    @Test
    @DisplayName("the constructor copies bytes; the accessor returns a copy")
    @Tag("testId:mirrornode.transaction.TransactionInfo/bytes/copies-in")
    void shouldCopyBytesIn() {
        final byte[] value = bytesValue();
        final byte[] expected = value.clone();
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), value, chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.bytes());
        final byte[] returned = subject.bytes();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.bytes());
    }

    @Test
    @DisplayName("chargedTxFee: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/chargedTxFee/accepts-at-minimum")
    void shouldAcceptChargedTxFeeAtMinimum() {
        final long value = -9223372036854775808L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), value, consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        assertValue(value, subject.chargedTxFee());
    }

    @Test
    @DisplayName("chargedTxFee: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/chargedTxFee/accepts-at-maximum")
    void shouldAcceptChargedTxFeeAtMaximum() {
        final long value = 9223372036854775807L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), value, consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        assertValue(value, subject.chargedTxFee());
    }

    @Test
    @DisplayName("rejects null for consensusTimestamp")
    @Tag("testId:mirrornode.transaction.TransactionInfo/consensusTimestamp/rejects-null")
    void shouldRejectNullConsensusTimestamp() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), (ZonedDateTime) null, entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("highVolumePricingMultiplier: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/highVolumePricingMultiplier/accepts-at-minimum")
    void shouldAcceptHighVolumePricingMultiplierAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), value, maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        assertValue(value, subject.highVolumePricingMultiplier());
    }

    @Test
    @DisplayName("highVolumePricingMultiplier: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/highVolumePricingMultiplier/accepts-at-maximum")
    void shouldAcceptHighVolumePricingMultiplierAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), value, maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        assertValue(value, subject.highVolumePricingMultiplier());
    }

    @Test
    @DisplayName("rejects null for maxCustomFees")
    @Tag("testId:mirrornode.transaction.TransactionInfo/maxCustomFees/rejects-null")
    void shouldRejectNullMaxCustomFees() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), (List<CustomFeeLimit>) null, maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies maxCustomFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/maxCustomFees/copies-in")
    void shouldCopyMaxCustomFeesIn() {
        final List<CustomFeeLimit> value = new ArrayList<>(maxCustomFeesValue());
        final List<CustomFeeLimit> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), value, maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        value.clear();
        assertValue(expected, subject.maxCustomFees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.maxCustomFees()).clear());
    }

    @Test
    @DisplayName("maxFee: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/maxFee/accepts-at-minimum")
    void shouldAcceptMaxFeeAtMinimum() {
        final long value = -9223372036854775808L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), value, memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        assertValue(value, subject.maxFee());
    }

    @Test
    @DisplayName("maxFee: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/maxFee/accepts-at-maximum")
    void shouldAcceptMaxFeeAtMaximum() {
        final long value = 9223372036854775807L;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), value, memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        assertValue(value, subject.maxFee());
    }

    @Test
    @DisplayName("rejects null for memo")
    @Tag("testId:mirrornode.transaction.TransactionInfo/memo/rejects-null")
    void shouldRejectNullMemo() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), (byte[]) null, nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies memo; the accessor returns a copy")
    @Tag("testId:mirrornode.transaction.TransactionInfo/memo/copies-in")
    void shouldCopyMemoIn() {
        final byte[] value = memoValue();
        final byte[] expected = value.clone();
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), value, nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
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
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), (TransactionType) null, nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("nonce: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/nonce/accepts-at-minimum")
    void shouldAcceptNonceAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), value, nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("nonce: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.transaction.TransactionInfo/nonce/accepts-at-maximum")
    void shouldAcceptNonceAtMaximum() {
        final int value = 2147483647;
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), value, nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        assertValue(value, subject.nonce());
    }

    @Test
    @DisplayName("rejects null for result")
    @Tag("testId:mirrornode.transaction.TransactionInfo/result/rejects-null")
    void shouldRejectNullResult() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), (String) null, scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("rejects null for validDuration")
    @Tag("testId:mirrornode.transaction.TransactionInfo/validDuration/rejects-null")
    void shouldRejectNullValidDuration() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), (Duration) null, validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("rejects null for validStartTimestamp")
    @Tag("testId:mirrornode.transaction.TransactionInfo/validStartTimestamp/rejects-null")
    void shouldRejectNullValidStartTimestamp() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), (ZonedDateTime) null, transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("rejects null for transfers")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transfers/rejects-null")
    void shouldRejectNullTransfers() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), (List<Transfer>) null, tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies transfers; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/transfers/copies-in")
    void shouldCopyTransfersIn() {
        final List<Transfer> value = new ArrayList<>(transfersValue());
        final List<Transfer> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), value, tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        value.clear();
        assertValue(expected, subject.transfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.transfers()).clear());
    }

    @Test
    @DisplayName("rejects null for tokenTransfers")
    @Tag("testId:mirrornode.transaction.TransactionInfo/tokenTransfers/rejects-null")
    void shouldRejectNullTokenTransfers() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), (List<TokenTransfer>) null, nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies tokenTransfers; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/tokenTransfers/copies-in")
    void shouldCopyTokenTransfersIn() {
        final List<TokenTransfer> value = new ArrayList<>(tokenTransfersValue());
        final List<TokenTransfer> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), value, nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
        value.clear();
        assertValue(expected, subject.tokenTransfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.tokenTransfers()).clear());
    }

    @Test
    @DisplayName("rejects null for nftTransfers")
    @Tag("testId:mirrornode.transaction.TransactionInfo/nftTransfers/rejects-null")
    void shouldRejectNullNftTransfers() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), (List<NftTransfer>) null, stakingRewardTransfersValue(), assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies nftTransfers; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/nftTransfers/copies-in")
    void shouldCopyNftTransfersIn() {
        final List<NftTransfer> value = new ArrayList<>(nftTransfersValue());
        final List<NftTransfer> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), value, stakingRewardTransfersValue(), assessedCustomFeesValue());
        value.clear();
        assertValue(expected, subject.nftTransfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.nftTransfers()).clear());
    }

    @Test
    @DisplayName("rejects null for stakingRewardTransfers")
    @Tag("testId:mirrornode.transaction.TransactionInfo/stakingRewardTransfers/rejects-null")
    void shouldRejectNullStakingRewardTransfers() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), (List<StakingRewardTransfer>) null, assessedCustomFeesValue()));
    }

    @Test
    @DisplayName("the constructor copies stakingRewardTransfers; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/stakingRewardTransfers/copies-in")
    void shouldCopyStakingRewardTransfersIn() {
        final List<StakingRewardTransfer> value = new ArrayList<>(stakingRewardTransfersValue());
        final List<StakingRewardTransfer> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), value, assessedCustomFeesValue());
        value.clear();
        assertValue(expected, subject.stakingRewardTransfers());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.stakingRewardTransfers()).clear());
    }

    @Test
    @DisplayName("rejects null for assessedCustomFees")
    @Tag("testId:mirrornode.transaction.TransactionInfo/assessedCustomFees/rejects-null")
    void shouldRejectNullAssessedCustomFees() {
        assertThrows(NullPointerException.class, () -> new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), (List<AssessedCustomFee>) null));
    }

    @Test
    @DisplayName("the constructor copies assessedCustomFees; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.TransactionInfo/assessedCustomFees/copies-in")
    void shouldCopyAssessedCustomFeesIn() {
        final List<AssessedCustomFee> value = new ArrayList<>(assessedCustomFeesValue());
        final List<AssessedCustomFee> expected = List.copyOf(value);
        final TransactionInfo subject = new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), value);
        value.clear();
        assertValue(expected, subject.assessedCustomFees());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.assessedCustomFees()).clear());
    }

    private static TransactionId transactionIdValue() {
        return TransactionId.generateTransactionId(new Address(0L, 0L, "", 1001L));
    }

    private static byte[] transactionHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static Authority batchKeyValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static byte[] bytesValue() {
        return new byte[] {1, 2, 3};
    }

    private static long chargedTxFeeValue() {
        return 1L;
    }

    private static ZonedDateTime consensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Address entityIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static boolean highVolumeValue() {
        return true;
    }

    private static long highVolumePricingMultiplierValue() {
        return 1L;
    }

    private static List<CustomFeeLimit> maxCustomFeesValue() {
        return List.of(new CustomFeeLimit(null, 1L, null));
    }

    private static long maxFeeValue() {
        return 1L;
    }

    private static byte[] memoValue() {
        return new byte[] {1, 2, 3};
    }

    private static TransactionType nameValue() {
        return BasicTransactionType.ATOMIC_BATCH;
    }

    private static int nonceValue() {
        return 1;
    }

    private static AccountId nodeValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static ZonedDateTime parentConsensusTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static String resultValue() {
        return "value";
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
        return List.of(new NftTransfer(true, new NftTransferParties(null, null, new Address(0L, 0L, "", 1001L)), 1L));
    }

    private static List<StakingRewardTransfer> stakingRewardTransfersValue() {
        return List.of(new StakingRewardTransfer(new AccountId(0L, 0L, "", 1001L, null, null), 1L));
    }

    private static List<AssessedCustomFee> assessedCustomFeesValue() {
        return List.of(new AssessedCustomFee(1L, null, List.of(new AccountId(0L, 0L, "", 1001L, null, null)), null));
    }

    private static TransactionInfo create() {
        return new TransactionInfo(transactionIdValue(), transactionHashValue(), batchKeyValue(), bytesValue(), chargedTxFeeValue(), consensusTimestampValue(), entityIdValue(), highVolumeValue(), highVolumePricingMultiplierValue(), maxCustomFeesValue(), maxFeeValue(), memoValue(), nameValue(), nonceValue(), nodeValue(), parentConsensusTimestampValue(), resultValue(), scheduledValue(), validDurationValue(), validStartTimestampValue(), transfersValue(), tokenTransfersValue(), nftTransfersValue(), stakingRewardTransfersValue(), assessedCustomFeesValue());
    }
}
