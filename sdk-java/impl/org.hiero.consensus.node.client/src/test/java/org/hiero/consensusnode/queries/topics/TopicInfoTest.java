package org.hiero.consensusnode.queries.topics;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `TopicInfo`, generated from the specs.
@DisplayName("TopicInfo")
class TopicInfoTest {

    @Test
    @DisplayName("creates a TopicInfo and returns the values")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/create")
    void shouldCreateAndReturnTheValues() {
        final Address topicId = topicIdValue();
        final String topicMemo = topicMemoValue();
        final byte[] runningHash = runningHashValue();
        final long sequenceNumber = sequenceNumberValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority submitAuthority = submitAuthorityValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final TopicInfo subject = new TopicInfo(topicId, topicMemo, runningHash, sequenceNumber, expirationTime, adminAuthority, submitAuthority, autoRenewPeriod, autoRenewAccount);
        assertValue(topicId, subject.topicId());
        assertValue(topicMemo, subject.topicMemo());
        assertValue(runningHash, subject.runningHash());
        assertValue(sequenceNumber, subject.sequenceNumber());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(submitAuthority, subject.submitAuthority());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
    }

    @Test
    @DisplayName("creates a TopicInfo with the default values")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final TopicInfo subject = new TopicInfo(topicIdValue(), runningHashValue(), sequenceNumberValue(), expirationTimeValue(), adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue());
        assertValue("", subject.topicMemo());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Address topicId = topicIdValue();
        final String topicMemo = topicMemoValue();
        final byte[] runningHash = runningHashValue();
        final long sequenceNumber = sequenceNumberValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable Authority submitAuthority = submitAuthorityValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final TopicInfo subject = TopicInfo.builder()
                .topicId(topicId)
                .topicMemo(topicMemo)
                .runningHash(runningHash)
                .sequenceNumber(sequenceNumber)
                .expirationTime(expirationTime)
                .adminAuthority(adminAuthority)
                .submitAuthority(submitAuthority)
                .autoRenewPeriod(autoRenewPeriod)
                .autoRenewAccount(autoRenewAccount)
                .build();
        assertValue(topicId, subject.topicId());
        assertValue(topicMemo, subject.topicMemo());
        assertValue(runningHash, subject.runningHash());
        assertValue(sequenceNumber, subject.sequenceNumber());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(submitAuthority, subject.submitAuthority());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final TopicInfo subject = TopicInfo.builder()
                .topicId(topicIdValue())
                .runningHash(runningHashValue())
                .sequenceNumber(sequenceNumberValue())
                .expirationTime(expirationTimeValue())
                .build();
        assertValue("", subject.topicMemo());
        assertNull(subject.adminAuthority());
        assertNull(subject.submitAuthority());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.autoRenewAccount());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> TopicInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for topicId")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/topicId/rejects-null")
    void shouldRejectNullTopicId() {
        assertThrows(NullPointerException.class, () -> new TopicInfo((Address) null, topicMemoValue(), runningHashValue(), sequenceNumberValue(), expirationTimeValue(), adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue()));
    }

    @Test
    @DisplayName("rejects null for topicMemo")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/topicMemo/rejects-null")
    void shouldRejectNullTopicMemo() {
        assertThrows(NullPointerException.class, () -> new TopicInfo(topicIdValue(), (String) null, runningHashValue(), sequenceNumberValue(), expirationTimeValue(), adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue()));
    }

    @Test
    @DisplayName("rejects null for runningHash")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/runningHash/rejects-null")
    void shouldRejectNullRunningHash() {
        assertThrows(NullPointerException.class, () -> new TopicInfo(topicIdValue(), topicMemoValue(), (byte[]) null, sequenceNumberValue(), expirationTimeValue(), adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue()));
    }

    @Test
    @DisplayName("the constructor copies runningHash; the accessor returns a copy")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/runningHash/copies-in")
    void shouldCopyRunningHashIn() {
        final byte[] value = runningHashValue();
        final byte[] expected = value.clone();
        final TopicInfo subject = new TopicInfo(topicIdValue(), topicMemoValue(), value, sequenceNumberValue(), expirationTimeValue(), adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.runningHash());
        final byte[] returned = subject.runningHash();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.runningHash());
    }

    @Test
    @DisplayName("sequenceNumber: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/sequenceNumber/accepts-at-minimum")
    void shouldAcceptSequenceNumberAtMinimum() {
        final long value = -9223372036854775808L;
        final TopicInfo subject = new TopicInfo(topicIdValue(), topicMemoValue(), runningHashValue(), value, expirationTimeValue(), adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue());
        assertValue(value, subject.sequenceNumber());
    }

    @Test
    @DisplayName("sequenceNumber: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/sequenceNumber/accepts-at-maximum")
    void shouldAcceptSequenceNumberAtMaximum() {
        final long value = 9223372036854775807L;
        final TopicInfo subject = new TopicInfo(topicIdValue(), topicMemoValue(), runningHashValue(), value, expirationTimeValue(), adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue());
        assertValue(value, subject.sequenceNumber());
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    @Tag("testId:consensusnode.queries.topics.TopicInfo/expirationTime/rejects-null")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new TopicInfo(topicIdValue(), topicMemoValue(), runningHashValue(), sequenceNumberValue(), (ZonedDateTime) null, adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue()));
    }

    private static Address topicIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static String topicMemoValue() {
        return "value";
    }

    private static byte[] runningHashValue() {
        return new byte[] {1, 2, 3};
    }

    private static long sequenceNumberValue() {
        return 1L;
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority submitAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TopicInfo create() {
        return new TopicInfo(topicIdValue(), topicMemoValue(), runningHashValue(), sequenceNumberValue(), expirationTimeValue(), adminAuthorityValue(), submitAuthorityValue(), autoRenewPeriodValue(), autoRenewAccountValue());
    }
}
